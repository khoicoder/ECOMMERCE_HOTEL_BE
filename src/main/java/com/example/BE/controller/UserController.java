package com.example.BE.controller;

import com.example.BE.dto.*;

import com.example.BE.services.AuthService;
import com.example.BE.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        return ResponseEntity.ok(userService
                .getProfile(authentication));
    }

    @PutMapping("/profile-update")
    public ResponseEntity<UpdateProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest rq,
                                                               Authentication authentication ) {
        return ResponseEntity.ok(userService.updateProfile(rq,authentication));
        }
    @PutMapping("/users/me/password")
    public ResponseEntity<ChangePasswordResponse>  changePassword(@Valid @RequestBody ChangePasswordRequest rq, Authentication authentication) {
        return ResponseEntity.ok(userService.changePassword(rq,authentication));
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
