package com.example.BE.services;

import com.example.BE.dto.user.response.AuthResponse;
import com.example.BE.dto.user.request.RegisterRequest;
import com.example.BE.enums.Role;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.exception.ValidateException;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.UserSessionRepository;
import com.example.BE.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Data
public class AuthService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final JwtUtil jwtUtil;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private final UserSessionRepository userSessionRepository;

    @Transactional
    public AuthResponse login(String username, String password, String devideId) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new NotFoundException("Wrong password");
        }
        if (user.getRole() == null) {
            throw new UnauthorizedException("User role is missing");
        }

        String refreshToken = jwtUtil.generateRefreshToken();
        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceID(devideId);
        session.setRefreshTokenHash(jwtUtil.hashtoken(refreshToken));
        session.setCreateAt(Instant.now());
        session.setLastUsedAt(Instant.now());
        session.setRefreshTokenExpireAt(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()));
        userSessionRepository.save(session);
        String accessToken = jwtUtil.generateAccessToken(user, session.getId());

        // Refresh Token Stateful with Redis (safe try-catch if Redis is optional/offline)
        try {
            redisTemplate.opsForValue().set(
                    refreshKey(user.getUsername()),
                    refreshToken, 7, TimeUnit.DAYS
            );
        } catch (Exception ex) {
            System.err.println("Redis unavailable, skipped Redis caching: " + ex.getMessage());
        }

        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }

    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null ||
                registerRequest.getEmail() == null
        ) {
            throw new NotFoundException("Missing data");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }
        UserModel user = new UserModel();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);
        user.setActive(true);

        userRepository.save(user);
        return "User registered successfully";
    }

    private String refreshKey(String username) {
        return "Refresh:" + username;
    }

    public String logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new ValidateException("Invalid refresh token");
        }

        String token = authHeader.substring(7);
        UUID sessionId = jwtUtil.extractSessionID(token);
        UserSession session = userSessionRepository.findById(sessionId).orElseThrow(()
                -> new NotFoundException("Invalid session ID"));
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
            userSessionRepository.save(session);
        }

        SecurityContextHolder.clearContext();
        return "logged out successfully";
    }

    @Transactional
    public String logoutAll(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new ValidateException("invalid token");
        }
        String token = authHeader.substring(7).trim();
        Long userId = jwtUtil.extractUserID(token);
        userSessionRepository.revokeAllActiveSessions(userId, Instant.now());
        SecurityContextHolder.clearContext();
        return "logged out all devic successfully";
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidateException("Refresh token is required");
        }
        String refreshTokenHash = jwtUtil.hashtoken(refreshToken);
        UserSession session = userSessionRepository
                .findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(()
                -> new ValidateException("Invalid refresh token"));
        if (session.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token đã bị thu hồi");
        }
        if (session.getRefreshTokenExpireAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh Token đã hết hạn");
        }
        UserModel user = session.getUser();
        String newRefreshToken = jwtUtil.generateRefreshToken();
        session.setRefreshTokenHash(jwtUtil.hashtoken(newRefreshToken));
        session.setLastUsedAt(Instant.now());
        session.setRefreshTokenExpireAt(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()));
        userSessionRepository.save(session);
        String newAccessToken = jwtUtil.generateAccessToken(user, session.getId());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole());
    }
}
