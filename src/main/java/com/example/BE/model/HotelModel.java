package com.example.BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double rating;

    @Column(name = "price_from")
    private Double priceFrom;

    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private List<RoomModel> roomModels;

}