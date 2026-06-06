package com.example.BE.dto.admin.response;

public record HotelResponse (
        Long hotelId,
        String hotelName,
        String hotelAddress,
        Double rate,
        String description


){}


