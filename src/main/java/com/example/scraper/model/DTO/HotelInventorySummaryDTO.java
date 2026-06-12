package com.example.scraper.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelInventorySummaryDTO {

  @JsonProperty("cheapest_rate_display")
  @JsonAlias("cheapestRateDisplay")
  private RateDisplayDTO cheapestRateDisplay;

}
