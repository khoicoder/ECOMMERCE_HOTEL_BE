package com.example.BE.repository;

import com.example.BE.model.HotelModel;
import com.example.BE.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<HotelModel, Long> {
    long count();
    HotelModel getHotelById(Long hotelId);
    boolean existsByNameIgnoreCase(String name);
    List<HotelModel> findByOwner(UserModel owner);
    List<HotelModel> findByOwner_Id(Long ownerId);
    Optional<HotelModel> findFirstByOwner_Id(Long ownerId);
}
