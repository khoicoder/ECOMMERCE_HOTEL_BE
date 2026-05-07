package com.example.BE.dto;

import com.example.BE.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private Role role;
}
