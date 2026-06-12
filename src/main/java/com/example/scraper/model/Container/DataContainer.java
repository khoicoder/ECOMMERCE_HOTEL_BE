package com.example.scraper.model.Container;

import com.example.scraper.model.DTO.HotelEntryDTO;
import lombok.Data;
import java.util.List;
@Data
public class DataContainer {
    private List<HotelEntryDTO> entries;
}