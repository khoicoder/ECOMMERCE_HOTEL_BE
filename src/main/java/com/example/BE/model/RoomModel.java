package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.example.BE.enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "rooms")
public class RoomModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonBackReference
    private HotelModel hotel;

}