package com.example.BE.controller;
// Admin1@
import com.example.BE.dto.user.response.AuthResponse;
import com.example.BE.dto.user.request.LoginRequest;
import com.example.BE.dto.user.request.RefreshRequest;
import com.example.BE.dto.user.request.RegisterRequest;
import com.example.BE.repository.UserRepository;
import com.example.BE.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.BE.security.JwtUtil;

import java.util.UUID;

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
    public AuthResponse login(@RequestBody LoginRequest request) {
        String deviceId = request.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString();
        }
        return authService.login(request.getUsername(),request.getPassword(),deviceId);

    }
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {

        return authService.logout(authHeader);
    }
    @PostMapping("/logout-all")
    public String logoutAll(@RequestHeader("Authorization") String authHeader) {
        return authService.logoutAll(authHeader);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest rq) {
        return authService.register(rq);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request){
        return authService.refreshAccessToken(
                request.getRefreshToken()
        );
    }


    //authentication pipeline
    //security context lifecycle
    //stateless session architecture




    @GetMapping("/me")
    public String me(){

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        System.out.println(auth);

        System.out.println(auth.getName());

        return auth.getName();
    }
}