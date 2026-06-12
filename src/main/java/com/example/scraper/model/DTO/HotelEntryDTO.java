package com.example.scraper.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelEntryDTO {

    private String id;

    private String name;

    @JsonProperty("display_name")
    @JsonAlias("displayName")
    private String displayName;

    @JsonProperty("star_rating")
    @JsonAlias("starRating")
    private String starRating;

    @JsonProperty("user_rating")
    @JsonAlias("userRating")
    private String userRating;

    private String region;

    private String address;

    @JsonProperty("low_rate")
    @JsonAlias("lowRate")
    private String lowRate;

    @JsonProperty("image_url")
    @JsonAlias("imageUrl")
    private String imageUrl;

    @JsonProperty("image_links")
    @JsonAlias("imageLinks")
    private List<String> imageLinks;

    @JsonProperty("hotel_inventory_summary")
    @JsonAlias("hotelInventorySummary")
    private HotelInventorySummaryDTO hotelInventorySummary;

    @JsonProperty("hotel_seo_url")
    @JsonAlias("hotelSeoUrl")
    private String hotelSeoUrl;

    @JsonProperty("accom_property_type")
    @JsonAlias("accomPropertyType")
    private String accomPropertyType;

}
