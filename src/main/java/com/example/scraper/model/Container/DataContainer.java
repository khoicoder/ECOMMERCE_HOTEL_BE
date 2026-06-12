package com.example.scraper.model.Container;

import com.example.scraper.model.DTO.HotelEntryDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataContainer {

    private List<HotelEntryDTO> entries;

    @JsonProperty("searchResults")
    @JsonAlias("search_results")
    private List<HotelEntryDTO> searchResults;

    @JsonProperty("hotelList")
    @JsonAlias("hotel_list")
    private List<HotelEntryDTO> hotelList;

    @JsonProperty("searchList")
    @JsonAlias("search_list")
    private List<HotelEntryDTO> searchList;

    @JsonProperty("searchResultDisplay")
    @JsonAlias("search_result_display")
    private SearchResultDisplay searchResultDisplay;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResultDisplay {
        private List<HotelEntryDTO> entries;
    }

    public List<HotelEntryDTO> resolveEntries() {
        if (entries != null && !entries.isEmpty()) {
            return entries;
        }
        if (searchResults != null && !searchResults.isEmpty()) {
            return searchResults;
        }
        if (hotelList != null && !hotelList.isEmpty()) {
            return hotelList;
        }
        if (searchList != null && !searchList.isEmpty()) {
            return searchList;
        }
        if (searchResultDisplay != null
                && searchResultDisplay.getEntries() != null
                && !searchResultDisplay.getEntries().isEmpty()) {
            return searchResultDisplay.getEntries();
        }
        return List.of();
    }
}
