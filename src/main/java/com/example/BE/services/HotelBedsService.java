package com.example.BE.services;

import com.example.BE.config.HotelBedsConfig;
import com.example.BE.security.HotelBedsSignature;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor

public class HotelBedsService {
    private final HotelBedsConfig hotelBedsConfig;
    private final RestClient restClient;
    private final HotelBedsSignature hotelBedsSignature;
    public final String getHotelStatus(){
        return restClient.get()

                .uri(hotelBedsConfig.getBaseUrl()
                        + "/hotel-content-api/1.0/types/hotelStatuses")

                .header("Api-key", hotelBedsConfig.getApiKey())

                .header("X-Signature", hotelBedsSignature.generateSignature())

                .header("Accept", "application/json")

                .retrieve()

                .body(String.class);
    }
}
