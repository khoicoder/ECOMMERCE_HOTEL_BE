package com.example.BE.controller;

import com.example.BE.dto.LoginRequest;
import com.example.BE.dto.RegisterRequest;
import com.example.BE.model.UserModel;
import com.example.BE.repository.UserRepository;
import com.example.BE.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.BE.security.JwtUtil;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Data
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
            String Token = authService.login(request.getUsername(), request.getPassword());
            return Map.of("token .....=", Token);

    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest rq) {

        return authService.register(rq);
    }
}