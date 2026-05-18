package com.example.BE.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateProfileResponse {
    private String message;
    private ProfileResponse profileResponse;
    private String accessToken;
    private String refreshToken;
}
