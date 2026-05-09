package com.example.BE.controller;
// Admin1@
import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.LoginRequest;
import com.example.BE.dto.RefreshRequest;
import com.example.BE.dto.RegisterRequest;
import com.example.BE.repository.UserRepository;
import com.example.BE.services.AuthService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.BE.security.JwtUtil;

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
        return authService.login(request.getUsername(), request.getPassword());


    }
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {

        return authService.logout(authHeader);
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