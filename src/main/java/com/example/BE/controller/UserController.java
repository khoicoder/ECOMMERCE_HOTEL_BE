package com.example.BE.controller;

import com.example.BE.dto.ProfileResponse;
import com.example.BE.model.UserModel;
import com.example.BE.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;


    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        if (!isAuthenticate(authentication)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();
        System.out.println("username from token : " + username);
        System.out.println("AUTH = " + authentication);
        System.out.println("AUTH IS AUTHENTICATED = " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("USERNAME = " + username);
        UserModel user = getUserOrNull(username);
        if(user == null){
            return ResponseEntity.status(404).body("user not found" +username);
        }
        return ResponseEntity.ok(BuildProfileResponse(user)

        );
    }
    private boolean isAuthenticate(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();


    }
    public UserModel getUserOrNull(String username) {

        Optional<UserModel> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()){
            return null;
        }
        return optionalUser.get();
    }
    private ProfileResponse BuildProfileResponse(UserModel user) {
        return new ProfileResponse(user.getUsername(), user.getEmail(), user.getRole(), user.getPhone(), user.getAddress(), user.getAvatarUrl());
    }


//ET /api/profile
//↓
//JwtFilter chạy trước
//↓
//Nếu token hợp lệ, JwtFilter set Authentication
//↓
//Controller nhận Authentication
//↓
//isAuthenticated() check có login chưa
//↓
//Lấy username từ token
//↓
//Query DB theo username
//↓
//Nếu không có user → 404
//↓
//Nếu có user → convert UserModel sang ProfileResponse
//↓
//Trả về 200


}
