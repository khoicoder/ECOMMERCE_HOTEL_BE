package com.example.BE.controller;

import com.example.BE.model.HotelModel;
import com.example.BE.services.HotelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "http://localhost:3000")

public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<HotelModel> getHotels() {
        return hotelService.getAll();
    }
    @PostMapping
    public HotelModel create(@RequestBody HotelModel hotelModel) {
        return hotelService.create(hotelModel);
    }
}