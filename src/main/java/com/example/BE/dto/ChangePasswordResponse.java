package com.example.BE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordResponse {
    private String message;
    private ProfileResponse profile;
    private String accessToken;
    private String refreshToken;

}
