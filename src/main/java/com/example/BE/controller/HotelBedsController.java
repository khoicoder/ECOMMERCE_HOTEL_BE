package com.example.BE.controller;



import com.example.BE.services.HotelBedsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotelbeds")
@RequiredArgsConstructor
public class HotelBedsController {
    private final HotelBedsService hotelBedsService;
    @GetMapping("/status")
    public String status() {
        return hotelBedsService.getHotelStatus();
    }


    }

