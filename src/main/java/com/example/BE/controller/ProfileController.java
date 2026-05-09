package com.example.BE.controller;

import com.example.BE.dto.AuthResponse;
import com.example.BE.dto.UpdateProfileRequest;
import com.example.BE.model.UserModel;
import com.example.BE.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {
    private final AuthService authService;
    @PutMapping("/profile-update")
    public AuthResponse updateProfile(@Valid @RequestBody UpdateProfileRequest rq,
                                      Authentication authentication ) {
        return authService.updateProfile(rq,authentication);
    }
}
