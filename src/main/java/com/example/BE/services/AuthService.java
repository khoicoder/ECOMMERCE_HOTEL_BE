package com.example.BE.services;

import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.RegisterRequest;
import com.example.BE.enums.Role;
import com.example.BE.model.UserModel;
import com.example.BE.security.JwtUtil;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;
import org.springframework.web.bind.annotation.RestController;
//Admin1@
@Service
@Data
@RestController
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    public AuthResponse login(String username, String password) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        System.out.println(
                "Access Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(accessToken)
        );

        System.out.println(
                "Refresh Token sống còn: "
                        + jwtUtil.getRemainingTimeFormatted(refreshToken)
        );



        return new AuthResponse(accessToken, refreshToken);
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
    public AuthResponse refreshAccessToken(String refreshToken){
        if(!jwtUtil.validateToken(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }
        String username  = jwtUtil.extractUsername(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(refreshToken);
        return new AuthResponse(newAccessToken, refreshToken);


    }

}
