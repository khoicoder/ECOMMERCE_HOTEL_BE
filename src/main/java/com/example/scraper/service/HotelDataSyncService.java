package com.example.scraper.service;

import com.example.scraper.client.TravelokaApiClient;
import com.example.scraper.model.DTO.HotelEntryDTO;
import com.example.scraper.model.Response.TravelokaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;


@Service

public class HotelDataSyncService {
    @Autowired
    private TravelokaApiClient travelokaApiClient;
    @Autowired
    private ObjectMapper objectMapper;
    public void SyncHotel() {
        try {
            // 1. Lấy dữ liệu thô
            String jsonResponse = travelokaApiClient.fetchDataAsString();

            // --- CHỖ NÀY LÀ QUAN TRỌNG ĐỂ CHECK ---
            System.out.println("--- RAW JSON RESPONSE START ---");
            System.out.println(jsonResponse.substring(0, Math.min(jsonResponse.length(), 500))); // In 500 ký tự đầu
            System.out.println("--- RAW JSON RESPONSE END ---");

            // 2. Parse JSON
            TravelokaResponse response = objectMapper.readValue(jsonResponse, TravelokaResponse.class);

            // 3. Kiểm tra xem có dữ liệu không
            if (response.getData() == null || response.getData().getEntries() == null) {
                System.out.println("CẢNH BÁO: Không tìm thấy entry nào trong JSON!");
                return;
            }

            for (HotelEntryDTO dto : response.getData().getEntries()) {
                System.out.println("Đang lưu khách sạn: " + dto.getName() + " | ID: " + dto.getId());
            }

            System.out.println("Đồng bộ thành công!");
        } catch (Exception e) {
            System.err.println("LỖI ĐỒNG BỘ: " + e.getMessage());
            e.printStackTrace();
        }
    }


}