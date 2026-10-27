package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_code", columnList = "code", unique = true),
        @Index(name = "idx_tenant_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantModel extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code; // e.g., "DEFAULT", "VINPEARL", "MUONG_THANH"

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, TRIAL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;
}
