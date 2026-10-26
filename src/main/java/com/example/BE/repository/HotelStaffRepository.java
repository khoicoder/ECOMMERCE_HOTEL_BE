package com.example.BE.repository;

import com.example.BE.model.HotelModel;
import com.example.BE.model.HotelStaffModel;
import com.example.BE.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelStaffRepository extends JpaRepository<HotelStaffModel, Long> {
    List<HotelStaffModel> findByUser(UserModel user);
    List<HotelStaffModel> findByUserId(Long userId);
    List<HotelStaffModel> findByHotel(HotelModel hotel);
    Optional<HotelStaffModel> findFirstByUserId(Long userId);
}
