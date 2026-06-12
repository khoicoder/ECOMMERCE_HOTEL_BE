package com.example.scraper.controller;

import com.example.scraper.service.HotelDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
public class CrawlController {
    @Autowired
    private HotelDataSyncService hotelDataSyncService;

    @GetMapping("/run")
    public String triggerCrawl(){
        hotelDataSyncService.SyncHotel();
        return "Crawl job started!...............";
    }
}
