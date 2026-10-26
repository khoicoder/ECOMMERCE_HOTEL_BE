package com.example.BE.config;

import com.example.BE.enums.Role;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(HotelRepository hotelRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String encoded = passwordEncoder.encode("Admin123@");
            userRepository.findAll().forEach(u -> {
                u.setPassword(encoded);
                u.setActive(true);
                if (u.getUsername().equals("admin")) {
                    u.setRole(Role.SUPER_ADMIN);
                } else if (u.getUsername().equals("khoi") || u.getUsername().equals("admin_danang") || u.getUsername().equals("admin_dalat")) {
                    u.setRole(Role.ADMIN);
                }
                userRepository.save(u);
            });
            System.out.println(">> All database users successfully updated with password: Admin123@ and active: true");
        };
    }
}
