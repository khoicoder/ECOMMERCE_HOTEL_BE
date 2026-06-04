package com.example.BE.services;

import com.example.BE.dto.user.request.ChangePasswordRequest;
import com.example.BE.dto.user.request.UpdateProfileRequest;
import com.example.BE.dto.user.response.ChangePasswordResponse;
import com.example.BE.dto.user.response.ProfileResponse;
import com.example.BE.dto.user.response.UpdateProfileResponse;
import com.example.BE.exception.*;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.UserRepository;
import com.example.BE.repository.UserSessionRepository;
import com.example.BE.security.AuthPrincipal;
import com.example.BE.security.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public ChangePasswordResponse changePassword(
            ChangePasswordRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        AuthPrincipal principal =
                (AuthPrincipal) authentication.getPrincipal();

        UserModel user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {
            throw new ConflictException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {
            throw new ConflictException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        UserSession currentSession = sessionRepository
                .findById(principal.sessionId())
                .orElseThrow(() -> new BadRequestException("Session not found"));

        if (currentSession.getRevokedAt() != null) {
            throw new BadRequestException("Session đã bị đăng xuất");
        }

        if (currentSession.getRefreshTokenExpireAt().isBefore(Instant.now())) {
            throw new BadRequestException("Session đã hết hạn");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        if (Boolean.TRUE.equals(request.getLogoutOtherDevices())) {
            sessionRepository.revokeAllActiveSessionsExcept(
                    user.getId(),
                    currentSession.getId(),
                    Instant.now()
            );
        }

        String newRefreshToken = jwtUtil.generateRefreshToken();

        currentSession.setRefreshTokenHash(
                jwtUtil.hashtoken(newRefreshToken)
        );
        currentSession.setLastUsedAt(Instant.now());

        currentSession.setRefreshTokenExpireAt(
                Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration())
        );

        sessionRepository.save(currentSession);

        String newAccessToken = jwtUtil.generateAccessToken(
                user,
                currentSession.getId()
        );

        return new ChangePasswordResponse(
                "Đổi mật khẩu thành công",
                BuildProfileResponse(user),
                newAccessToken,
                newRefreshToken
        );
    }
    //lấy đại diện
    private AuthPrincipal getPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new ValidateException("Invalid authentication principal");
        }

        return authPrincipal;
    }




    public ProfileResponse getProfile(Authentication authentication) {
        AuthPrincipal principal = getPrincipal(authentication);
        UserModel user = userRepository.findById(principal.userId()).orElseThrow(()
                -> new NotFoundException("User not found"));
        return BuildProfileResponse(user);

    }
    @Transactional
    public UpdateProfileResponse updateProfile(
            UpdateProfileRequest request,
            Authentication authentication) {

        UserModel user = getCurrentUser(authentication);
        AuthPrincipal principal = getPrincipal(authentication);
        validateBlankFields(request);
        boolean usernameChanged =
                request.getUsername() != null
                        && !request.getUsername().isBlank()
                        && !request.getUsername().equals(user.getUsername());

        boolean emailChanged =
                request.getEmail() != null
                        && !request.getEmail().isBlank()
                        && !request.getEmail().equals(user.getEmail());

        boolean avatarChanged =
                request.getAvatarUrl() != null
                        && !request.getAvatarUrl().isBlank();

        boolean phoneChanged =
                request.getPhone() != null
                        && !request.getPhone().isBlank()
                        && !request.getPhone().equals(user.getPhone());

        boolean addressChanged =
                request.getAddress() != null
                        && !request.getAddress().isBlank()
                        && !request.getAddress().equals(user.getAddress());


        boolean needNewToken =
                usernameChanged
                        || emailChanged;

        if (usernameChanged) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new ConflictException("Username already exists");
            }

            user.setUsername(request.getUsername());
        }

        if (emailChanged) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ConflictException("Email already exists");
            }

            user.setEmail(request.getEmail());
        }

        if (avatarChanged) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (phoneChanged) {
            user.setPhone(request.getPhone());
        }

        if (addressChanged) {
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        String accessToken = null;
        String refreshToken = null;
        if (needNewToken) {
            UserSession currentSesson = sessionRepository.findById(principal.sessionId()).orElseThrow(()
                    ->new UnauthorizedException("Session not found"));
            if(currentSesson.getRevokedAt() != null) {
                throw new UnauthorizedException("Session đã bị đăng xuất");
            }
            if(currentSesson.getRefreshTokenExpireAt().isBefore(Instant.now())) {
                throw new UnauthorizedException("Session đã hết hạn");
            }
            refreshToken = jwtUtil.generateRefreshToken();
            currentSesson.setRefreshTokenHash(jwtUtil.hashtoken(refreshToken));
            currentSesson.setLastUsedAt(Instant.now());
            currentSesson.setRefreshTokenExpireAt(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()));
            sessionRepository.save(currentSesson);
            accessToken = jwtUtil.generateAccessToken(user, currentSesson.getId());
            return new UpdateProfileResponse(
                    "cập nhật hồ sơ thành công",
                    BuildProfileResponse(user),
                    accessToken,
                    refreshToken
            );
        }


        return new UpdateProfileResponse(
                "profile updated successfully",
                BuildProfileResponse(user),
                accessToken,
                refreshToken
        );
    }

    private static Long getId(UserModel user) {
        return user.getId();
    }


    private ProfileResponse BuildProfileResponse(UserModel user) {
        return new ProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl());
    }
    private UserModel getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");

        }
        AuthPrincipal principal =  (AuthPrincipal) auth.getPrincipal();
        System.out.println("AUTH NAME = " + auth.getName());
        System.out.println("PRINCIPAL = " + auth.getPrincipal());


        return userRepository.findById(principal.userId()).orElseThrow(()
                -> new NotFoundException("User not found :"+principal.userId()));

    }
    private void validateBlankFields(UpdateProfileRequest rq) {
        if (rq.getUsername() != null && rq.getUsername().isBlank()) {
            throw new ValidateException("Username không được để trống");
        }

        if (rq.getEmail() != null && rq.getEmail().isBlank()) {
            throw new ValidateException("Email không được để trống");
        }

        if (rq.getPhone() != null && rq.getPhone().isBlank()) {
            throw new ValidateException("Phone không được để trống");
        }

        if (rq.getAddress() != null && rq.getAddress().isBlank()) {
            throw new ValidateException("Address không được để trống");
        }

        if (rq.getAvatarUrl() != null && rq.getAvatarUrl().isBlank()) {
            throw new ValidateException("Avatar không được để trống");
        }

    }
    private String refreshKey(String username) {
        return "refresh" + username;
    }
}
