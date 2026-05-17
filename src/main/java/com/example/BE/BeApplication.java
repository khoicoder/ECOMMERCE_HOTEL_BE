package com.example.BE;

import com.example.BE.config.SecurityConfig;
import com.example.BE.dto.ProfileResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication


public class BeApplication {


	public static void main(String[] args) {

		SpringApplication.run(BeApplication.class, args);

	}

}
//
//[ ] GlobalExceptionHandler
//	[ ] ApiErrorResponse
//[ ] UserService
//[ ] ProfileResponse không lộ UserModel
//[ ] Redis key refresh token thống nhất
//[ ] Logout xóa đúng refresh token
//[ ] Refresh token check Redis đúng
//[ ] Axios refresh flow
//[ ] Role authority trong JwtFilter
//[ ] Admin endpoint
//[ ] Change password endpoint riêng
//[ ] Upload avatar
//[ ] Hotel CRUD
//[ ] Room CRUD
//[ ] Booking
//[ ] QR Payment timeout