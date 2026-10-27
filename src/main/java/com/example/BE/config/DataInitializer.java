package com.example.BE.config;

import com.example.BE.enums.Role;
import com.example.BE.model.HotelModel;
import com.example.BE.model.TenantModel;
import com.example.BE.model.UserModel;
import com.example.BE.repository.HotelRepository;
import com.example.BE.repository.TenantRepository;
import com.example.BE.repository.UserRepository;
import com.example.BE.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Configuration
public class DataInitializer {

    @Value("${app.multitenancy.mode:shared}")
    private String multitenancyMode;

    @Value("${app.multitenancy.default-tenant:DEFAULT}")
    private String defaultTenantCode;

    @Bean
    CommandLineRunner initDatabase(
            TenantRepository tenantRepository,
            HotelRepository hotelRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            boolean isDedicated = "dedicated".equalsIgnoreCase(multitenancyMode);
            TenantContext.setDedicatedMode(isDedicated);
            TenantContext.setDefaultTenantCode(defaultTenantCode);
            log.info("[DataInitializer] Chế độ triển khai Multi-tenancy: {} (Default Tenant: {})",
                    isDedicated ? "DEDICATED" : "SHARED_SAAS", defaultTenantCode);

            // 1. Initialize or get Default Tenant for backward compatibility and Shared SaaS
            TenantModel defaultTenant = tenantRepository.findByCode(defaultTenantCode).orElseGet(() -> {
                TenantModel t = new TenantModel();
                t.setCode(defaultTenantCode);
                t.setName(isDedicated ? "Dedicated Organization" : "Vietnam Luxury Hospitality Group");
                t.setStatus("ACTIVE");
                t.setDescription("Tổ chức chuỗi khách sạn tiêu chuẩn");
                t.setContactEmail("admin@vietnamhotels.vn");
                t.setContactPhone("0909999999");
                TenantModel saved = tenantRepository.save(t);
                log.info("[DataInitializer] Đã khởi tạo Tenant mặc định: {} (ID: {})", saved.getName(), saved.getId());
                return saved;
            });

            // 2. Link existing hotels without tenant to default tenant
            List<HotelModel> hotels = hotelRepository.findAll();
            for (HotelModel h : hotels) {
                if (h.getTenant() == null) {
                    h.setTenant(defaultTenant);
                    hotelRepository.save(h);
                }
            }

            // 3. Ensure essential seed admin accounts exist if database is fresh
            if (userRepository.count() == 0) {
                String encodedPassword = passwordEncoder.encode("Admin123@");
                UserModel superAdmin = new UserModel();
                superAdmin.setUsername("admin");
                superAdmin.setEmail("admin@vietnamhotels.vn");
                superAdmin.setPassword(encodedPassword);
                superAdmin.setRole(Role.SUPER_ADMIN);
                superAdmin.setActive(true);
                superAdmin.setTenant(defaultTenant);
                userRepository.save(superAdmin);
                log.info("[DataInitializer] Cơ sở dữ liệu trống: Đã khởi tạo Super Admin ban đầu (admin)");
            } else {
                // Ensure the 'admin' account has SUPER_ADMIN role if not set, WITHOUT overwriting password
                userRepository.findByUsername("admin").ifPresent(adminUser -> {
                    if (adminUser.getRole() != Role.SUPER_ADMIN) {
                        adminUser.setRole(Role.SUPER_ADMIN);
                        userRepository.save(adminUser);
                    }
                });
            }

            log.info("[DataInitializer] Khởi tạo hệ thống Multi-tenant và Scope dữ liệu thành công.");
        };
    }
}
