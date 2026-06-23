package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_logs")
public class AuditLogModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long actorId;
    private String actionName;
    private String action;
    private String entityType;

    private Long entityId;
    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt;

}
