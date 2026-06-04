package com.example.BE.services;

import com.example.BE.dto.admin.request.ChangeRoleRequest;
import com.example.BE.dto.admin.response.UserResponse;
import com.example.BE.dto.user.response.ProfileResponse;
import com.example.BE.enums.Role;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.model.UserModel;
import com.example.BE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    @Override
    public Page<UserResponse> getAllUsers(String keyword, String role, boolean active, int page, int size) {
        Pageable  pageable =PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(this :: mapToResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserModel user = userRepository.findById(id).orElseThrow(()-> new NotFoundException("User not found"+id));

        return mapToResponse(user);

    }


    @Override
    public UserResponse changeRole(Long id,ChangeRoleRequest request) {
        UserModel user  = userRepository.findById(id).orElseThrow(()
                -> new NotFoundException("User not found"));
        if(user.getRole().equals(Role.ADMIN)){
            System.out.println("HELLO AMIN");
        }
        if (user.getRole() == request.role()) {
            throw new BadRequestException(
                    "User already has role " + request.role()
            );
        }
        user.setRole(request.role());
        userRepository.save(user);
        return mapToResponse(user)
        ;
    }


    private UserResponse mapToResponse(UserModel userModel) {
        return new UserResponse(
            userModel.getId(),
            userModel.getUsername(),
            userModel.getEmail(),
            userModel.getRole().name(),
            userModel.getPhone(),
            userModel.getAddress(),
            userModel.isActive(),
            userModel.getCreatedAt(),
            userModel.getUpdatedAt());

    }


}
