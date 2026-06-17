package com.example.BE.dto.user.request;


import java.time.LocalDate;
import java.time.LocalDateTime;


public record BookingRequest(
       Long roomId,
       LocalDateTime checkInDate,
       LocalDateTime checkOutDate


        ){

}