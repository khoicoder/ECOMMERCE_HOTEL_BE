package com.example.BE.model;

import com.example.BE.audit.BaseAuditable;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelModel extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    private Double rating;

    @Column(name = "price_from")
    private Double priceFrom;

    private String image;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)

    @JsonManagedReference
    private List<RoomModel> rooms;

}