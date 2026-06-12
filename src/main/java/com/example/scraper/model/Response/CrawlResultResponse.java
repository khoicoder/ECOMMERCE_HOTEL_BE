package com.example.scraper.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResultResponse {

    private int fetched;
    private int saved;
    private int skipped;
    private String message;

}
