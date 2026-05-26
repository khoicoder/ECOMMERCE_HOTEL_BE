package com.example.BE.services;
import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.RegisterRequest;

import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.UserSessionRepository;
import com.example.BE.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//Admin1@
@Service
@Data
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserSessionRepository userSessionRepository;

    @Transactional
    public AuthResponse login(String username, String password, String devideId) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        if (user.getRole() == null
        ) {
            throw new RuntimeException("User role is missing");
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
        //Refresh Token Stateful
        redisTemplate.opsForValue().set(
                refreshKey(user.getUsername()),
                refreshToken, 7, TimeUnit.DAYS

        );
        System.out.println(
                "Access Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(accessToken)
        );


        System.out.println(
                "Refresh Token đã tạo dạng opaque token, không parse JWT expiration"
        );


        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole());
    }

    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null
                || registerRequest.getEmail() == null
        ) {
            throw new RuntimeException("Missing data");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        UserModel user = new UserModel();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);

        userRepository.save(user);
        return "User registered successfully";
    }

    private String refreshKey(String username) {
        return "Refresh:" + username;
    }

    public String logout(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new RuntimeException("invalid token");
        }

        String token = authHeader.substring(7);
        UUID sessionId = jwtUtil.extractSessionID(token);
        UserSession session = userSessionRepository.findById(sessionId).orElseThrow(()
                -> new RuntimeException("Invalid session ID")) ;
        if(session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
            userSessionRepository.save(session);
        }

        SecurityContextHolder.clearContext();
        return "logged out successfully";

    }
    @Transactional
    public String logoutAll(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new RuntimeException("invalid token");
        }
        String token = authHeader.substring(7).trim();
        Long userId = jwtUtil.extractUserID(token);
        userSessionRepository.revokeAllActiveSessions(userId,Instant.now());
        SecurityContextHolder.clearContext();
        return "logged out all devic successfully";


    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresah token is required");

        }
        // hash refresh token từ client gửi lên
        String refreshTokenHash = jwtUtil.hashtoken(refreshToken);
        // tìm session theo hash
        UserSession session = userSessionRepository
                .findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(()
                -> new RuntimeException("Invalid refresh token"));
        //check token da bi thu hoi chua
        if (session.getRevokedAt() != null) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }
        if (session.getRefreshTokenExpireAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh Token đã hết hạn");

        }
        UserModel user = session.getUser();
        String newRefreshToken = jwtUtil.generateRefreshToken();
        //update hash mới vào db
        session.setRefreshTokenHash(jwtUtil.hashtoken(newRefreshToken));
        //update time sử dụng lần cuối vào db
        session.setLastUsedAt(Instant.now());
        //cập nhật thời gian hết hạn mới
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