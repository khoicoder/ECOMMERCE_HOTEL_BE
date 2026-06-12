package com.example.scraper.controller;

import com.example.scraper.model.Response.CrawlResultResponse;
import com.example.scraper.service.HotelDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlController {

    private final HotelDataSyncService hotelDataSyncService;

    @GetMapping("/run")
    public CrawlResultResponse triggerCrawl() {
        return hotelDataSyncService.syncHotels();
    }
}
