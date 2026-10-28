package com.example.BE.services;

import com.example.BE.dto.user.request.RegisterRequest;
import com.example.BE.dto.user.response.AuthResponse;
import com.example.BE.enums.Role;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.exception.ValidateException;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.UserRepository;
import com.example.BE.repository.UserSessionRepository;
import com.example.BE.security.JwtUtil;
import com.example.BE.security.SessionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionRepository userSessionRepository;
    private final SessionCacheService sessionCacheService;

    @Transactional
    public AuthResponse login(String username, String password, String deviceId) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new NotFoundException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        if (!user.isActive()) {
            throw new UnauthorizedException("Tài khoản đã bị tạm khóa. Vui lòng liên hệ quản trị viên");
        }
        if (user.getRole() == null) {
            throw new UnauthorizedException("Tài khoản chưa được phân quyền");
        }

        // 1. Authoritative Session Persistence in MySQL
        String refreshToken = jwtUtil.generateRefreshToken();
        Instant now = Instant.now();
        Instant refreshTokenExpireAt = now.plusMillis(jwtUtil.getRefreshTokenExpiration());

        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceID(deviceId != null && !deviceId.isBlank() ? deviceId : UUID.randomUUID().toString());
        session.setRefreshTokenHash(jwtUtil.hashtoken(refreshToken));
        session.setCreateAt(now);
        session.setLastUsedAt(now);
        session.setRefreshTokenExpireAt(refreshTokenExpireAt);
        UserSession savedSession = userSessionRepository.save(session);

        // 2. Generate Access Token containing sessionId ('sid') and tenantCode ('tid')
        String accessToken = jwtUtil.generateAccessToken(user, savedSession.getId());

        // 3. Fast-path Session Cache in Redis
        String roleStr = user.getRole().name();
        String tenantCode = user.getTenant() != null ? user.getTenant().getCode() : "DEFAULT";
        sessionCacheService.cacheSession(
                savedSession.getId(),
                user.getId(),
                roleStr,
                tenantCode,
                refreshTokenExpireAt
        );

        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }

    @Transactional
    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null ||
                registerRequest.getEmail() == null
        ) {
            throw new ValidateException("Vui lòng điền đầy đủ thông tin bắt buộc");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new ConflictException("Tên đăng nhập '" + registerRequest.getUsername() + "' đã tồn tại!");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ConflictException("Email '" + registerRequest.getEmail() + "' đã được sử dụng!");
        }

        UserModel user = new UserModel();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);
        user.setActive(true);

        userRepository.save(user);
        return "Đăng ký tài khoản thành công";
    }

    @Transactional
    public String logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidateException("Authorization header không hợp lệ hoặc thiếu tiền tố 'Bearer '");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ValidateException("Token không được để trống");
        }

        UUID sessionId = jwtUtil.extractSessionIDAllowExpired(token);
        Long userId = jwtUtil.extractUserIDAllowExpired(token);
        if (sessionId == null || userId == null) {
            throw new ValidateException("Token không hợp lệ hoặc thiếu thông tin định danh phiên");
        }

        UserSession session = userSessionRepository.findById(sessionId)
                .orElse(null);

        if (session != null) {
            if (!session.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("Phiên làm việc này không thuộc về tài khoản của bạn");
            }

            // 1. Authoritative Revocation in MySQL
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                userSessionRepository.save(session);
            }
        }

        // 2. Immediate Revocation in Redis Fast-Cache
        sessionCacheService.revokeSession(sessionId);

        SecurityContextHolder.clearContext();
        return "Đăng xuất thành công";
    }

    @Transactional
    public String logoutAll(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidateException("Authorization header không hợp lệ hoặc thiếu tiền tố 'Bearer '");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ValidateException("Token không được để trống");
        }

        Long userId = jwtUtil.extractUserIDAllowExpired(token);
        if (userId == null) {
            throw new ValidateException("Token không chứa thông tin user");
        }

        // Query active sessions before revocation to cleanly invalidate them in Redis
        List<UserSession> userSessions = userSessionRepository.findByUserId(userId);
        List<UUID> activeSessionIds = userSessions.stream()
                .filter(s -> s.getRevokedAt() == null)
                .map(UserSession::getId)
                .toList();

        // 1. Authoritative Bulk Revoke in MySQL
        userSessionRepository.revokeAllActiveSessions(userId, Instant.now());

        // 2. Invalidate Cached Sessions in Redis
        sessionCacheService.revokeSessions(activeSessionIds);

        SecurityContextHolder.clearContext();
        return "Đã đăng xuất khỏi toàn bộ các thiết bị";
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidateException("Refresh token không được để trống");
        }

        String oldHash = jwtUtil.hashtoken(refreshToken.trim());
        UserSession session = userSessionRepository
                .findByRefreshTokenHash(oldHash)
                .orElseThrow(() -> new ValidateException("Refresh token không hợp lệ hoặc không tồn tại"));

        if (session.getRevokedAt() != null) {
            throw new UnauthorizedException("Phiên làm việc đã bị thu hồi trước đó. Vui lòng đăng nhập lại");
        }

        Instant now = Instant.now();
        if (session.getRefreshTokenExpireAt().isBefore(now)) {
            throw new UnauthorizedException("Phiên làm việc (Refresh Token) đã hết hạn. Vui lòng đăng nhập lại");
        }

        UserModel user = session.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("Tài khoản người dùng đã bị khóa");
        }

        // Prepare new rotation tokens
        String newRefreshToken = jwtUtil.generateRefreshToken();
        String newHash = jwtUtil.hashtoken(newRefreshToken);
        Instant newExpireAt = now.plusMillis(jwtUtil.getRefreshTokenExpiration());

        // 1. Defend Against Concurrent Refresh Token Rotation Race Condition (Atomic Conditional DB Update)
        int updated = userSessionRepository.rotateRefreshToken(
                session.getId(),
                oldHash,
                newHash,
                newExpireAt,
                now
        );

        if (updated == 0) {
            log.warn("[AuthService] Race condition detected for session {}. Concurrent refresh rejected.", session.getId());
            throw new UnauthorizedException("Refresh token đã được sử dụng hoặc bị quay vòng bởi yêu cầu đồng thời khác");
        }

        // 2. Generate new Access Token
        String newAccessToken = jwtUtil.generateAccessToken(user, session.getId());

        // 3. Update Redis Fast-Cache
        String roleStr = user.getRole() != null ? user.getRole().name() : "USER";
        String tenantCode = user.getTenant() != null ? user.getTenant().getCode() : "DEFAULT";
        sessionCacheService.cacheSession(
                session.getId(),
                user.getId(),
                roleStr,
                tenantCode,
                newExpireAt
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole()
        );
    }
}
