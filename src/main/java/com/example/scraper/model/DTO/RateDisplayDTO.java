package com.example.scraper.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RateDisplayDTO {

  @JsonProperty("total_fare")
  @JsonAlias("totalFare")
  private MoneyAmountDTO totalFare;

  @JsonProperty("base_fare")
  @JsonAlias("baseFare")
  private MoneyAmountDTO baseFare;

}
