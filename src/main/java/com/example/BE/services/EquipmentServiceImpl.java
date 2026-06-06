package com.example.BE.services;

import com.example.BE.repository.EquipmentRepository;
import com.example.BE.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class EquipmentServiceImpl {
    private  EquipmentRepository equipmentRepository;
    private UserRepository userRepository;



}
