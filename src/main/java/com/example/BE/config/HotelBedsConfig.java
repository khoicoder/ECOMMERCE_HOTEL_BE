package com.example.BE.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hotelbeds")
@Getter
@Setter
public class HotelBedsConfig {
    private String apiKey;
    private String secret;
    private String baseUrl;

}
