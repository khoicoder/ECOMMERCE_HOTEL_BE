package com.example.BE.services;

import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.HotelModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public List<HotelModel> getAll() {
        return hotelRepository.findAll();
    }

    public HotelModel getHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Hotel not found: " + id));
    }

    public HotelModel create(HotelModel hotelModel, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            UserModel user = userRepository.findById(principal.userId()).orElse(null);
            hotelModel.setOwner(user);
        }
        return hotelRepository.save(hotelModel);
    }

    public HotelModel update(Long id, HotelModel updated, Authentication authentication) {
        HotelModel existing = getHotelById(id);
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            if (principal.role() != com.example.BE.enums.Role.SUPER_ADMIN) {
                if (existing.getOwner() == null || !existing.getOwner().getId().equals(principal.userId())) {
                    throw new UnauthorizedException("You can only edit your own hotel");
                }
            }
        }
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getImage() != null) existing.setImage(updated.getImage());
        if (updated.getPriceFrom() != null) existing.setPriceFrom(updated.getPriceFrom());
        if (updated.getRating() != null) existing.setRating(updated.getRating());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        return hotelRepository.save(existing);
    }

    public void delete(Long id, Authentication authentication) {
        HotelModel existing = getHotelById(id);
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            if (principal.role() != com.example.BE.enums.Role.SUPER_ADMIN) {
                throw new UnauthorizedException("Chỉ Super Admin mới có quyền xóa khách sạn");
            }
        }
        hotelRepository.delete(existing);
    }
}
