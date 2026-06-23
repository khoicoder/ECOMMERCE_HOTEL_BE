package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "maintenance_history")
public class MaintenanceHistoryModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_id")
    private MaintenanceModel maintenance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private UserModel changeBy;
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus oldStatus;
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus newStatus;
    private String description;
}
