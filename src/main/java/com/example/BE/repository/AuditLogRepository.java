package com.example.BE.repository;

import com.example.BE.model.AuditLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogModel, Long> {
    List<AuditLogModel> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
