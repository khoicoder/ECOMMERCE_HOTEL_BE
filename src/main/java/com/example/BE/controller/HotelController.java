package com.example.BE.controller;

import com.example.BE.model.HotelModel;
import com.example.BE.services.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<HotelModel>> getHotels() {
        return ResponseEntity.ok(hotelService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelModel> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HotelModel> create(@RequestBody HotelModel hotelModel, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.create(hotelModel, authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HotelModel> update(
            @PathVariable Long id,
            @RequestBody HotelModel hotelModel,
            Authentication authentication
    ) {
        return ResponseEntity.ok(hotelService.update(id, hotelModel, authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        hotelService.delete(id, authentication);
        return ResponseEntity.ok().build();
    }
}
