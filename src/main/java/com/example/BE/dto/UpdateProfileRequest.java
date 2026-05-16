package com.example.BE.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    @Email(message = "Email kh đúng định dạng")
    private String email;
    private String avatar;
    private String address;
    private String phone;
    private String currentPassword;
    private String newPassword;

}
