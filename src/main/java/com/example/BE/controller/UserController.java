package com.example.BE.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        return "welcome "+authentication.getName();
    }

}
