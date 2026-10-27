package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "hotels", indexes = {
        @Index(name = "idx_hotel_tenant", columnList = "tenant_id"),
        @Index(name = "idx_hotel_status", columnList = "status"),
        @Index(name = "idx_hotel_owner", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelModel extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="hotel_id")
    private Long id;

    private String name;

    private String location;

    private Double rating;

    @Column(name = "price_from")
    private BigDecimal priceFrom;

    private String image;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    @JsonIgnore
    private TenantModel tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private UserModel owner;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RoomModel> rooms;
}
