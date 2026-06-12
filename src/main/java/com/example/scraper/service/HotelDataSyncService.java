package com.example.scraper.service;

import com.example.BE.model.HotelModel;
import com.example.BE.repository.HotelRepository;
import com.example.scraper.client.TravelokaApiClient;
import com.example.scraper.mapper.HotelCrawlMapper;
import com.example.scraper.model.Container.DataContainer;
import com.example.scraper.model.DTO.HotelEntryDTO;
import com.example.scraper.model.Response.CrawlResultResponse;
import com.example.scraper.model.Response.TravelokaResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelDataSyncService {

    private final TravelokaApiClient travelokaApiClient;
    private final ObjectMapper objectMapper;
    private final HotelCrawlMapper hotelCrawlMapper;
    private final HotelRepository hotelRepository;

    public CrawlResultResponse syncHotels() {
        try {
            String jsonResponse = travelokaApiClient.fetchHotelList();
            log.info("Traveloka response preview: {}", preview(jsonResponse));

            List<HotelEntryDTO> entries = parseEntries(jsonResponse);
            if (entries.isEmpty()) {
                return CrawlResultResponse.builder()
                        .fetched(0)
                        .saved(0)
                        .skipped(0)
                        .message("Không tìm thấy khách sạn trong response. Kiểm tra cookie Traveloka trong application.yaml.")
                        .build();
            }

            int saved = 0;
            int skipped = 0;

            for (HotelEntryDTO entry : entries) {
                HotelModel mapped = hotelCrawlMapper.toEntity(entry);
                if (!StringUtils.hasText(mapped.getName())) {
                    skipped++;
                    continue;
                }

                if (hotelRepository.existsByNameIgnoreCase(mapped.getName())) {
                    skipped++;
                    log.debug("Bỏ qua khách sạn đã tồn tại: {}", mapped.getName());
                    continue;
                }

                hotelRepository.save(mapped);
                saved++;
                log.info("Đã lưu: {} | Giá từ: {} | Địa điểm: {}",
                        mapped.getName(), mapped.getPriceFrom(), mapped.getLocation());
            }

            return CrawlResultResponse.builder()
                    .fetched(entries.size())
                    .saved(saved)
                    .skipped(skipped)
                    .message("Đồng bộ hoàn tất")
                    .build();
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Traveloka", e);
            return CrawlResultResponse.builder()
                    .fetched(0)
                    .saved(0)
                    .skipped(0)
                    .message("Lỗi: " + e.getMessage())
                    .build();
        }
    }

    private List<HotelEntryDTO> parseEntries(String jsonResponse) throws Exception {
        TravelokaResponse response = objectMapper.readValue(jsonResponse, TravelokaResponse.class);
        if (response.getData() != null) {
            List<HotelEntryDTO> entries = response.getData().resolveEntries();
            if (!entries.isEmpty()) {
                return entries;
            }
        }

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = root.path("data");
        List<HotelEntryDTO> catalogEntries = parseSearchListCatalogEntries(dataNode);
        if (!catalogEntries.isEmpty()) {
            return catalogEntries;
        }

        for (String field : List.of(
                "entries", "searchResults", "search_results", "hotelList", "hotel_list",
                "searchList", "search_list"
        )) {
            List<HotelEntryDTO> parsed = parseArrayNode(dataNode.path(field));
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }

        List<HotelEntryDTO> displayEntries = parseArrayNode(dataNode.path("searchResultDisplay").path("entries"));
        if (!displayEntries.isEmpty()) {
            return displayEntries;
        }

        return List.of();
    }

    private List<HotelEntryDTO> parseSearchListCatalogEntries(JsonNode dataNode) throws Exception {
        JsonNode entries = dataNode.path("entries");
        if (!entries.isArray() || entries.isEmpty()) {
            return List.of();
        }

        List<HotelEntryDTO> hotels = new ArrayList<>();
        for (JsonNode entry : entries) {
            String displayType = entry.path("displayType").asText("");
            if (!displayType.isEmpty() && !displayType.contains("INVENTORY")) {
                continue;
            }

            JsonNode hotelNode = entry.path("hotelInventorySummary").isMissingNode()
                    ? entry.path("inventorySummary")
                    : entry.path("hotelInventorySummary");

            if (hotelNode.isMissingNode()) {
                hotelNode = entry;
            }

            HotelEntryDTO dto = objectMapper.treeToValue(entry, HotelEntryDTO.class);
            if (!StringUtils.hasText(dto.getName())) {
                JsonNode nameNode = entry.path("displayName");
                if (nameNode.isMissingNode()) {
                    nameNode = entry.path("name");
                }
                if (!nameNode.isMissingNode()) {
                    dto.setName(nameNode.asText());
                    dto.setDisplayName(nameNode.asText());
                }
            }
            if (dto.getHotelInventorySummary() == null && !hotelNode.isMissingNode()) {
                dto.setHotelInventorySummary(
                        objectMapper.treeToValue(hotelNode, com.example.scraper.model.DTO.HotelInventorySummaryDTO.class)
                );
            }
            hotels.add(dto);
        }
        return hotels;
    }

    private List<HotelEntryDTO> parseArrayNode(JsonNode arrayNode) throws Exception {
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return List.of();
        }
        List<HotelEntryDTO> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            result.add(objectMapper.treeToValue(item, HotelEntryDTO.class));
        }
        return result;
    }

    private String preview(String json) {
        if (json == null) {
            return "";
        }
        return json.substring(0, Math.min(json.length(), 500));
    }
}
