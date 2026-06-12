package com.example.scraper.client;

import com.example.BE.exception.UnauthorizedException;
import com.example.scraper.config.TravelokaProperties;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TravelokaApiClient {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final DateTimeFormatter SPEC_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int MAX_POLL_ATTEMPTS = 20;
    private static final long POLL_INTERVAL_MS = 500;

    private final TravelokaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String fetchHotelList() throws Exception {
        bootstrapSessionIfNeeded();

        HttpResponse<String> response = sendSearchRequest(resolveRequestBody());
        detectWafChallenge(response);

        if (response.statusCode() == 200) {
            return response.body();
        }

        if (response.statusCode() == 202) {
            return pollSearchResults(response);
        }

        throw new UnauthorizedException(
                "Traveloka API lỗi HTTP " + response.statusCode() + ": " + preview(response.body())
        );
    }

    private void detectWafChallenge(HttpResponse<String> response) {
        if (response.statusCode() != 202) {
            return;
        }
        String wafAction = response.headers().firstValue("x-amzn-waf-action").orElse("");
        if ("challenge".equalsIgnoreCase(wafAction)) {
            throw new UnauthorizedException(
                    "Traveloka WAF chặn request (aws-waf challenge). "
                            + "Mở lại trang search Đà Lạt trên trình duyệt, copy cookie MỚI "
                            + "(đặc biệt aws-waf-token, datadome, tvl, tvs) vào application.yaml rồi chạy lại ngay."
            );
        }
    }

    private String resolveRequestBody() throws Exception {
        if (StringUtils.hasText(properties.getRequestBody())) {
            return properties.getRequestBody().trim();
        }
        if (StringUtils.hasText(properties.getRequestBodyFile())) {
            ClassPathResource resource = new ClassPathResource(properties.getRequestBodyFile());
            if (resource.exists()) {
                try (InputStream input = resource.getInputStream()) {
                    return new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
            }
        }
        return buildSearchRequestBody(null);
    }

    private String pollSearchResults(HttpResponse<String> initialResponse) throws Exception {
        String searchId = extractSearchId(initialResponse.body());
        String pollBodyTemplate = StringUtils.hasText(properties.getRequestBody())
                ? properties.getRequestBody().trim()
                : null;

        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            Thread.sleep(POLL_INTERVAL_MS);
            String pollBody = pollBodyTemplate != null
                    ? pollBodyTemplate
                    : buildSearchRequestBody(searchId);
            HttpResponse<String> pollResponse = sendSearchRequest(pollBody);
            detectWafChallenge(pollResponse);

            if (pollResponse.statusCode() == 200) {
                return pollResponse.body();
            }

            if (pollResponse.statusCode() == 202) {
                if (hasHotelEntries(pollResponse.body())) {
                    return pollResponse.body();
                }
                String nextSearchId = extractSearchId(pollResponse.body());
                if (StringUtils.hasText(nextSearchId)) {
                    searchId = nextSearchId;
                }
                continue;
            }

            throw new UnauthorizedException(
                    "Traveloka poll lỗi HTTP " + pollResponse.statusCode() + ": " + preview(pollResponse.body())
            );
        }

        throw new UnauthorizedException(
                "Traveloka poll timeout. searchId=" + searchId + ", body=" + preview(initialResponse.body())
        );
    }

    private HttpResponse<String> sendSearchRequest(String requestBody) throws Exception {
        String cookieHeader = resolveCookieHeader();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + properties.getSearchApiPath()))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("accept", "*/*")
                .header("accept-encoding", "gzip, deflate")
                .header("accept-language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("cache-control", "no-cache")
                .header("content-type", "application/json")
                .header("origin", properties.getBaseUrl())
                .header("referer", buildReferer())
                .header("user-agent", USER_AGENT)
                .header("x-client-interface", "mobile")
                .header("x-domain", "accomSearch")
                .header("x-route-prefix", properties.getRoutePrefix())
                .header("tv-country", "VN")
                .header("tv-currency", properties.getCurrency())
                .header("tv-language", "vi_VN")
                .header("cookie", cookieHeader);

        extractCookieValue(cookieHeader, "clientSessionId")
                .ifPresent(value -> requestBuilder.header("tv-clientsessionid", value));
        extractCookieValue(cookieHeader, "tv_mcc_id")
                .ifPresent(value -> requestBuilder.header("tv-mcc-id", value));

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void bootstrapSessionIfNeeded() throws Exception {
        if (StringUtils.hasText(properties.getCookie())) {
            return;
        }

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient bootstrapClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest bootstrapRequest = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/" + properties.getRoutePrefix() + "/hotel/vietnam/"))
                .header("user-agent", USER_AGENT)
                .header("accept-language", "vi-VN,vi;q=0.9")
                .GET()
                .build();

        bootstrapClient.send(bootstrapRequest, HttpResponse.BodyHandlers.discarding());
    }

    private String buildSearchRequestBody(String searchId) throws Exception {
        Occupancy occupancy = resolveOccupancy();

        ObjectNode data = objectMapper.createObjectNode();
        data.put("spec", resolveSearchSpec());
        data.put("currency", properties.getCurrency());
        data.put("locale", properties.getLocale());
        data.put("numAdults", occupancy.numAdults());
        data.put("numChildren", properties.getNumChildren());
        data.put("numRooms", occupancy.numRooms());
        data.put("numInfants", 0);
        data.put("page", properties.getPage());
        data.put("rowsPerPage", properties.getRowsPerPage());
        data.put("sid", "MAIN_FUNNEL");
        data.put("sort", "RECOMMENDED");

        ArrayNode rateTypes = data.putArray("rateTypes");
        rateTypes.add("PAY_NOW");
        rateTypes.add("PAY_AT_PROPERTY");

        ObjectNode filter = data.putObject("filter");
        filter.putNull("priceFilter");
        filter.putNull("starFilter");
        filter.putNull("facilityFilter");
        filter.putNull("accommodationTypeFilter");

        if (StringUtils.hasText(searchId)) {
            data.put("searchId", searchId);
        } else {
            data.putNull("searchId");
            data.putNull("prevSearchId");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.putArray("fields");
        root.set("data", data);
        root.put("clientInterface", "mobile");

        return objectMapper.writeValueAsString(root);
    }

    private String extractSearchId(String body) throws Exception {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        JsonNode root = objectMapper.readTree(body);
        for (String path : new String[]{"data/searchId", "data/search_id", "searchId"}) {
            JsonNode node = root.at("/" + path);
            if (!node.isMissingNode() && !node.isNull() && StringUtils.hasText(node.asText())) {
                return node.asText();
            }
        }
        return null;
    }

    private boolean hasHotelEntries(String body) throws Exception {
        if (!StringUtils.hasText(body)) {
            return false;
        }
        JsonNode root = objectMapper.readTree(body);
        for (String path : new String[]{
                "data/entries",
                "data/searchResults",
                "data/hotelList",
                "data/searchList",
                "data/searchResultDisplay/entries"
        }) {
            JsonNode node = root.at("/" + path);
            if (node.isArray() && !node.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String resolveSearchSpec() {
        if (StringUtils.hasText(properties.getSearchSpec())) {
            return URLDecoder.decode(properties.getSearchSpec().trim(), StandardCharsets.UTF_8);
        }

        LocalDate checkIn = LocalDate.now().plusDays(properties.getCheckInOffsetDays());
        LocalDate checkOut = checkIn.plusDays(properties.getStayNights());

        return String.format(
                "%s.%s.%d.%d.HOTEL_GEO.%s.%s.2",
                checkIn.format(SPEC_DATE),
                checkOut.format(SPEC_DATE),
                properties.getNumRooms(),
                properties.getNumAdults(),
                properties.getGeoId(),
                properties.getGeoName()
        );
    }

    private String resolveSearchSpecForReferer() {
        return toAsciiUrlComponent(resolveSearchSpec());
    }

    private String toAsciiUrlComponent(String value) {
        StringBuilder encoded = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            if (isUnreserved(b)) {
                encoded.append((char) b);
            } else {
                encoded.append('%').append(String.format("%02X", b));
            }
        }
        return encoded.toString();
    }

    private boolean isUnreserved(int b) {
        return (b >= 'a' && b <= 'z')
                || (b >= 'A' && b <= 'Z')
                || (b >= '0' && b <= '9')
                || b == '-' || b == '_' || b == '.' || b == '~';
    }

    private String resolveCookieHeader() {
        if (StringUtils.hasText(properties.getCookie())) {
            return properties.getCookie().trim();
        }

        String sessionId = "T1-web." + UUID.randomUUID().toString().replace("-", "").substring(0, 26).toUpperCase();
        return String.join("; ",
                "countryCode=VN",
                "clientSessionId=" + sessionId,
                "tv_cs=1",
                "tv_user={\"authorizationLevel\":100,\"id\":null}"
        );
    }

    private Occupancy resolveOccupancy() {
        if (!StringUtils.hasText(properties.getSearchSpec())) {
            return new Occupancy(properties.getNumRooms(), properties.getNumAdults());
        }

        String spec = URLDecoder.decode(properties.getSearchSpec().trim(), StandardCharsets.UTF_8);
        String[] parts = spec.split("\\.");
        if (parts.length >= 4) {
            try {
                return new Occupancy(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } catch (NumberFormatException ignored) {
                // fallback
            }
        }
        return new Occupancy(properties.getNumRooms(), properties.getNumAdults());
    }

    private String buildReferer() {
        return properties.getBaseUrl() + "/" + properties.getRoutePrefix()
                + "/hotel/search?spec=" + resolveSearchSpecForReferer();
    }

    private java.util.Optional<String> extractCookieValue(String cookieHeader, String name) {
        Pattern pattern = Pattern.compile("(?:^|;\\s*)" + Pattern.quote(name) + "=([^;]*)");
        Matcher matcher = pattern.matcher(cookieHeader);
        if (matcher.find()) {
            return java.util.Optional.of(matcher.group(1).trim());
        }
        return java.util.Optional.empty();
    }

    private String preview(String body) {
        if (body == null) {
            return "";
        }
        return body.substring(0, Math.min(body.length(), 500));
    }

    private record Occupancy(int numRooms, int numAdults) {}
}
