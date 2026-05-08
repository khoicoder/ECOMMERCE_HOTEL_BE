package com.example.BE.services;

import com.example.BE.config.RedisConfig;
import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.RegisterRequest;
import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import com.example.BE.security.JwtUtil;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;

import java.util.concurrent.TimeUnit;

//Admin1@
@Service
@Data
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;



    public AuthResponse login(String username, String password) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        //Refresh Token Stateful
        redisTemplate.opsForValue().set(
                "Refresh"+username,
                refreshToken, 7, TimeUnit.DAYS

        );
        System.out.println(
                "Access Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(accessToken)
        );

        System.out.println(
                "Refresh Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(refreshToken)
        );




        return new AuthResponse(accessToken, refreshToken, user.getUsername(),user.getRole());
    }
    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null ||
                registerRequest.getPassword() == null) {
            throw new RuntimeException("Missing data");
        }

        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
        throw new RuntimeException("Username already exists");
        }
        UserModel user = new UserModel();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        return "User registered successfully";
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
    public AuthResponse refreshAccessToken(String refreshToken){
        if(!jwtUtil.validateRefreshToken(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }
        String username  = jwtUtil.extractUsername(refreshToken);
        String savedToken = (String)redisTemplate.opsForValue().get("Refresh:"+username);
        if(savedToken==null || savedToken.equals(refreshToken)){
            throw new RuntimeException("User not found hoặc refresh token expired");
        }

        UserModel user = userRepository.findByUsername(username) .orElseThrow(()
                -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(username);

        return new AuthResponse(newAccessToken,refreshToken,
                user.getUsername(),
                user.getRole());

    }

}
