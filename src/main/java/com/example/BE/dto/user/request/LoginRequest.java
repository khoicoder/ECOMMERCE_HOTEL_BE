package com.example.BE.dto.user.request;


import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String deviceId;
}
