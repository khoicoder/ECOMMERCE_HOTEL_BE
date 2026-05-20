package com.example.BE.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name= "user_sessions")
@Data
public class UserSession {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private UserModel user;
    @Column(nullable = false)
    private String deviceID;
    @Column(nullable = false,length = 64,unique = true)
    private String refreshTokenHash;
    @Column(nullable = false)
    private Instant refreshTokenExpireAt;
    private Instant revokedAt;

    @Column(nullable = false)
    private Instant createAt;
    @Column(nullable = false)
    private Instant lastUsedAt;

    private String userAgent;
    private String ipAddress;


}
