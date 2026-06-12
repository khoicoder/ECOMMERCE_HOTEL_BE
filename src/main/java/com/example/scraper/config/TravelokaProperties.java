package com.example.scraper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "traveloka")
public class TravelokaProperties {

    private String baseUrl = "https://www.traveloka.com";
    private String routePrefix = "vi-vn";
    private String locale = "vi-VN";
    private String currency = "VND";

    /** Geo ID của Vietnam trên Traveloka (có thể đổi trong application.yaml). */
    private String geoId = "10000332";
    private String geoName = "Vietnam";

    private int numAdults = 2;
    private int numChildren = 0;
    private int numRooms = 1;
    private int page = 1;
    private int rowsPerPage = 25;

    /** Số ngày sau hôm nay để đặt check-in mặc định. */
    private int checkInOffsetDays = 7;
    private int stayNights = 1;

    /**
     * Cookie đầy đủ từ trình duyệt (F12 → Network → copy cookie header).
     * Bắt buộc khi Traveloka chặn bot (DataDome/WAF).
     */
    private String cookie = "";

    /** Ghi đè spec search; nếu để trống sẽ tự build từ geoId + ngày. */
    private String searchSpec = "";

    /** Endpoint search hotel (copy từ Network tab). */
    private String searchApiPath = "/api/v2/hotel/searchList";

    /**
     * Payload JSON đầy đủ từ tab Payload của request searchList.
     * Nếu có giá trị thì ưu tiên dùng thay cho body tự build.
     */
    private String requestBody = "";

    /** File JSON trên classpath, mặc định payload searchList Đà Lạt. */
    private String requestBodyFile = "traveloka/search-list-request.json";

}
