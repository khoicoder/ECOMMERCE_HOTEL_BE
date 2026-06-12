package com.example.scraper.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelEntryDTO {
    private String id;
    private String name;
    private String userRating;
    private String starRating;
    private String lowRate;
    private String region;
}