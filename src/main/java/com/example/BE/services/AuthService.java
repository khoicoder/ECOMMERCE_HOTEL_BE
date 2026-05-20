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
    public AuthResponse login(String username, String password,String devideId) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        if (user.getRole() == null
        ){
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
        String accessToken = jwtUtil.generateAccessToken(user,session.getId());
        //Refresh Token Stateful
        redisTemplate.opsForValue().set(
                refreshKey(user.getUsername()),
                refreshToken, 7, TimeUnit.DAYS

        );System.out.println(
                "Access Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(accessToken)
        );


        System.out.println(
                "Refresh Token đã tạo dạng opaque token, không parse JWT expiration"
        );



        return new AuthResponse(accessToken, refreshToken, user.getUsername(),user.getRole());
    }
    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null
                || registerRequest.getEmail() == null
    ) {
            throw new RuntimeException("Missing data");
        }

        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
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
        if(authHeader == null || !authHeader.startsWith("Bearer")) {
            throw new RuntimeException("invalid token");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        redisTemplate.delete("refresh"+username);
        SecurityContextHolder.clearContext();
        return "logged out successfully";


    }
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken){
        String refreshTokenHash = jwtUtil.hashtoken(refreshToken);
        UserSession session = userSessionRepository.findByRefreshTokenHash(refreshTokenHash).orElseThrow(()
                ->new RuntimeException("Invalid refresh token"));
        if(session.getRevokedAt()!=null) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }
        if(session.getRefreshTokenExpireAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh Token đã hết hạn");

        }

        UserModel user = session.getUser();
        String newRefreshToken = jwtUtil.generateRefreshToken();

        session.setRefreshTokenHash(jwtUtil.hashtoken(newRefreshToken));

        session.setLastUsedAt(Instant.now());
        session.getRefreshTokenExpireAt().plusMillis(jwtUtil.getRefreshTokenExpiration());
        userSessionRepository.save(session);
        String newAccessToken =jwtUtil.generateAccessToken(user,session.getId());



        return new AuthResponse(newAccessToken,newRefreshToken,
                user.getUsername(),
                user.getRole());

    }
//    public AuthResponse updateProfile(UpdateProfileRequest request,Authentication auth) {
//        if(auth == null || !auth.isAuthenticated()) {
//            throw new RuntimeException("Unauthorized");
//        }
//        String currentUsername  = auth.getName();
//        UserModel user =userRepository.findByUsername(currentUsername ).orElseThrow(()
//                -> new RuntimeException("User not found")
//        );
//        boolean usernameChanged =
//                request.getUsername() != null
//                && !request.getUsername().isBlank()
//                && !request.getUsername().equals(user.getUsername());
//        boolean emailChanged = request.getEmail() != null
//                && !request.getEmail().isBlank()
//                && request.getEmail().equals(user.getEmail());
//        boolean avatarChanged = request.getAvatar()!= null
//                && !request.getAvatar().isBlank();
//        boolean phoneChanged = request.getPhone()!= null
//                && !request.getPhone().isBlank()
//                && !request.getPhone().equals(user.getPhone());
//        boolean adressChanged = request.getAddress()!= null
//                && !request.getAddress().isBlank()
//                && !request.getAddress().equals(user.getAddress());
//        boolean passwordChanged = request.getNewPassword() != null
//                && !request.getNewPassword().isBlank();
//
//
//        if(usernameChanged){
//            if(userRepository.findByUsername(request.getUsername()).isPresent()) {
//                throw new RuntimeException("Username already exists");
//            }
//        user.setUsername(request.getUsername());}
//
//        if(emailChanged){
//            if(userRepository.findByEmail(request.getEmail()).isPresent()) {
//                throw new RuntimeException("Email already exists");
//
//            }
//            user.setEmail(request.getEmail());
//            }
//
//
//        if(avatarChanged)
//            user.setAvatarUrl(request.getAvatar());
//        if(phoneChanged)
//            user.setPhone(request.getPhone());
//        if(adressChanged)
//            user.setAddress(request.getAddress());
//
//        if(passwordChanged) {
//            if(request.getCurrentPassword() == null
//                    || request.getCurrentPassword().isBlank()
//                    || !passwordEncoder.matches(request.getCurrentPassword(),user.getPassword())) {
//                throw new RuntimeException("current password does not match");
//            }
//            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        }
//
//            userRepository.save(user);
//            String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
//            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
//            redisTemplate.opsForValue().set("Refresh"+user.getUsername(), newRefreshToken, 7, TimeUnit.DAYS);
//
//        return new AuthResponse(
//                newAccessToken
//                ,newRefreshToken
//                ,user.getUsername()
//                ,user.getRole());
//    }
    //identity consistency
    //security context lifecycle
    //token stale data problem ,Stale Token Problem "user experience + security + scalability"



}
