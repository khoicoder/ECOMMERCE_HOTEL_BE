package com.example.scraper.mapper;

import com.example.BE.model.HotelModel;
import com.example.scraper.model.DTO.HotelEntryDTO;
import com.example.scraper.model.DTO.HotelInventorySummaryDTO;
import com.example.scraper.model.DTO.MoneyAmountDTO;
import com.example.scraper.model.DTO.RateDisplayDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class HotelCrawlMapper {

    public HotelModel toEntity(HotelEntryDTO dto) {
        HotelModel hotel = new HotelModel();
        hotel.setName(resolveName(dto));
        hotel.setLocation(resolveLocation(dto));
        hotel.setRating(parseRating(dto.getUserRating()));
        hotel.setPriceFrom(resolvePrice(dto));
        hotel.setImage(resolveImage(dto));
        hotel.setStatus("ACTIVE");
        hotel.setDescription(buildDescription(dto));
        hotel.setRooms(new ArrayList<>());
        return hotel;
    }

    private String resolveName(HotelEntryDTO dto) {
        if (StringUtils.hasText(dto.getDisplayName())) {
            return dto.getDisplayName().trim();
        }
        if (StringUtils.hasText(dto.getName())) {
            return dto.getName().trim();
        }
        return null;
    }

    private String resolveLocation(HotelEntryDTO dto) {
        if (StringUtils.hasText(dto.getRegion())) {
            return dto.getRegion().trim();
        }
        if (StringUtils.hasText(dto.getAddress())) {
            return dto.getAddress().trim();
        }
        return "Vietnam";
    }

    private Double parseRating(String userRating) {
        if (!StringUtils.hasText(userRating)) {
            return null;
        }
        try {
            return Double.parseDouble(userRating.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private BigDecimal resolvePrice(HotelEntryDTO dto) {
        if (StringUtils.hasText(dto.getLowRate())) {
            return parseAmount(dto.getLowRate());
        }

        HotelInventorySummaryDTO summary = dto.getHotelInventorySummary();
        if (summary != null && summary.getCheapestRateDisplay() != null) {
            RateDisplayDTO rateDisplay = summary.getCheapestRateDisplay();
            if (rateDisplay.getTotalFare() != null) {
                BigDecimal total = parseAmount(rateDisplay.getTotalFare().getAmount());
                if (total != null) {
                    return total;
                }
            }
            if (rateDisplay.getBaseFare() != null) {
                return parseAmount(rateDisplay.getBaseFare().getAmount());
            }
        }
        return null;
    }

    private BigDecimal parseAmount(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim().replace(",", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String resolveImage(HotelEntryDTO dto) {
        if (StringUtils.hasText(dto.getImageUrl())) {
            return dto.getImageUrl();
        }
        List<String> links = dto.getImageLinks();
        if (links != null && !links.isEmpty() && StringUtils.hasText(links.getFirst())) {
            return links.getFirst();
        }
        return null;
    }

    private String buildDescription(HotelEntryDTO dto) {
        StringBuilder description = new StringBuilder("Nguồn: Traveloka");
        if (StringUtils.hasText(dto.getAccomPropertyType())) {
            description.append(" | Loại: ").append(dto.getAccomPropertyType());
        }
        if (StringUtils.hasText(dto.getStarRating())) {
            description.append(" | Sao: ").append(dto.getStarRating());
        }
        if (StringUtils.hasText(dto.getId())) {
            description.append(" | Traveloka ID: ").append(dto.getId());
        }
        if (StringUtils.hasText(dto.getHotelSeoUrl())) {
            description.append(" | URL: https://www.traveloka.com/vi-vn/").append(dto.getHotelSeoUrl());
        }
        return description.toString();
    }
}
