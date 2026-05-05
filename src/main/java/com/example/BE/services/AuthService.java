package com.example.BE.services;

import com.example.BE.dto.RegisterRequest;
import com.example.BE.model.UserModel;
import com.example.BE.security.JwtUtil;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.BE.repository.UserRepository;
@Service
@Data
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return jwtUtil.generateToken(username);
    }
    public String register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            throw new RuntimeException("Missing data");
        }


    if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
        throw new RuntimeException("Username already exists");
    }
    UserModel user = new UserModel();
    user.setUsername(registerRequest.getUsername());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));



    userRepository.save(user);
    return "User registered successfully";

    }


}
