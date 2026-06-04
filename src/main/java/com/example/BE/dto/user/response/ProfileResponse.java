package com.example.BE.dto.user.response;

import com.example.BE.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private String username;
    private String email;
    private Role role;
    private String phone;
    private String address;
    private String avatarUrl;
}
