package com.example.BE.dto;

import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    @Email(message = "Email kh đúng định dạng")
    private String email;
    private String avatar;
    private String address;
    @Pattern(
            regexp = "^(0[0-9]{9})$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;


}
