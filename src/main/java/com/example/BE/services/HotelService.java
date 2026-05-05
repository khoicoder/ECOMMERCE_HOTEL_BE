package com.example.BE.services;

import com.example.BE.model.HotelModel;

import com.example.BE.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<HotelModel> getAll() {
        return hotelRepository.findAll();
    }
    public HotelModel create(HotelModel hotelModel) {
        return hotelRepository.save(hotelModel);
    }

}