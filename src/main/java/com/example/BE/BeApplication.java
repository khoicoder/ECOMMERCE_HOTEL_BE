package com.example.BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")

@EnableJpaAuditing(auditorAwareRef = "applicationAuditorAware")
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