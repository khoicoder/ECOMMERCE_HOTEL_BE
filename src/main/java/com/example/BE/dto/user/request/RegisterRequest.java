package com.example.BE.dto.user.request;

import lombok.Data;

@Data
public class RegisterRequest {
    String username;
    String password;
    String email;

}
