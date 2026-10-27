package com.example.BE.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelAdminResponse {
    private Long hotelId;
    private String hotelName;
    private String hotelLocation;
    private boolean hasAdmin;
    private Long adminId;
    private String adminUsername;
    private String adminEmail;
    private String adminPhone;
    private Boolean adminActive;
    private long roomCount;
}
