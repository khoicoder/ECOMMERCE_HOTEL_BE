package com.example.BE.repository;

import com.example.BE.model.TenantModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantModel, Long> {
    Optional<TenantModel> findByCode(String code);
    boolean existsByCode(String code);
}
