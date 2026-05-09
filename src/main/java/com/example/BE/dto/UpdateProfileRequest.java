package com.example.BE.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Username không được để trống hoặc chỉ chứa khoảng trắng")
    private String username;
    @Email(message = "Email kh đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;
    private String currentPassword;
    private String newPassword;

}
