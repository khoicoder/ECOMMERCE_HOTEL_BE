package com.example.BE.services;

import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.ProfileResponse;
import com.example.BE.dto.UpdateProfileRequest;
import com.example.BE.model.UserModel;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;


    public ProfileResponse getProfile(Authentication authentication) {
        if (authentication == null|| !authentication.isAuthenticated() ) {
            throw new RuntimeException("Unauthorized");
        }
        String username = authentication.getName();
        UserModel user = userRepository.findByUsername(username).orElseThrow(()
                -> new RuntimeException("User not found :"+username));
        return BuildProfileResponse(user);

    }

    public AuthResponse updateProfile(
            UpdateProfileRequest request,
            Authentication authentication
    ) {
        UserModel user = getCurrentUser(authentication);

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
                request.getAvatar() != null
                        && !request.getAvatar().isBlank();

        boolean phoneChanged =
                request.getPhone() != null
                        && !request.getPhone().isBlank()
                        && !request.getPhone().equals(user.getPhone());

        boolean addressChanged =
                request.getAddress() != null
                        && !request.getAddress().isBlank()
                        && !request.getAddress().equals(user.getAddress());

        boolean passwordChanged =
                request.getNewPassword() != null
                        && !request.getNewPassword().isBlank();

        if (usernameChanged) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }

            user.setUsername(request.getUsername());
        }

        if (emailChanged) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }

            user.setEmail(request.getEmail());
        }

        if (avatarChanged) {
            user.setAvatarUrl(request.getAvatar());
        }

        if (phoneChanged) {
            user.setPhone(request.getPhone());
        }

        if (addressChanged) {
            user.setAddress(request.getAddress());
        }

        if (passwordChanged) {
            if (request.getCurrentPassword() == null
                    || request.getCurrentPassword().isBlank()) {
                throw new RuntimeException("Current password is required");
            }

            if (!passwordEncoder.matches(
                    request.getCurrentPassword(),
                    user.getPassword()
            )) {
                throw new RuntimeException("Current password does not match");
            }

            user.setPassword(
                    passwordEncoder.encode(request.getNewPassword())
            );
        }

        userRepository.save(user);

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        redisTemplate.opsForValue().set(
                user.getUsername(),
                newRefreshToken,
                7,
                TimeUnit.DAYS
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole()
        );
    }


    private ProfileResponse BuildProfileResponse(UserModel user) {
        return new ProfileResponse(user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl());
    }
    private UserModel getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");

        }
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found :"+username));

    }
    private void validateBlankFields(UpdateProfileRequest rq) {
        if (rq.getUsername() != null && rq.getUsername().isBlank()) {
            throw new RuntimeException("Username không được để trống");
        }

        if (rq.getEmail() != null && rq.getEmail().isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (rq.getPhone() != null && rq.getPhone().isBlank()) {
            throw new RuntimeException("Phone không được để trống");
        }

        if (rq.getAddress() != null && rq.getAddress().isBlank()) {
            throw new RuntimeException("Address không được để trống");
        }

        if (rq.getAvatar() != null && rq.getAvatar().isBlank()) {
            throw new RuntimeException("Avatar không được để trống");
        }

        if (rq.getCurrentPassword() != null && rq.getCurrentPassword().isBlank()) {
            throw new RuntimeException("Current password không được để trống");
        }

        if (rq.getNewPassword() != null && rq.getNewPassword().isBlank()) {
            throw new RuntimeException("New password không được để trống");
        }
    }
}
