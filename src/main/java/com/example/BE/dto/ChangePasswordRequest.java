package com.example.BE.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "mật khẩu hiện tại không được để trống")
    private String currentPassword;
    @NotBlank(message = "mật khẩu mới không được để trống")
    @Size(min =6,message = "mật khẩu có ít nhất 6 kí tự")
    private String newPassword;

    private Boolean logoutOtherDevices = true;

}
