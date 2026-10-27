package com.example.BE.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistResponse {
    private Long id;              // User ID
    private Long staffId;         // HotelStaff ID
    private String username;
    private String email;
    private String phone;
    private String address;
    private boolean active;
    private Long hotelId;
    private String hotelName;
    private Instant createdAt;
}
