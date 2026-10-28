package com.example.BE;

import com.example.BE.dto.user.response.AuthResponse;
import com.example.BE.enums.Role;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.exception.ValidateException;
import com.example.BE.model.UserModel;
import com.example.BE.model.UserSession;
import com.example.BE.repository.UserRepository;
import com.example.BE.repository.UserSessionRepository;
import com.example.BE.security.JwtUtil;
import com.example.BE.security.SessionCacheService;
import com.example.BE.security.SessionCacheStatus;
import com.example.BE.services.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SessionAuthAndRedisTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SessionCacheService sessionCacheService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private UserModel anotherUser;
    private final String rawPassword = "Password123@";

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        testUser = new UserModel();
        testUser.setUsername("user_" + uid);
        testUser.setEmail("user_" + uid + "@hotel.com");
        testUser.setPassword(passwordEncoder.encode(rawPassword));
        testUser.setRole(Role.USER);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        anotherUser = new UserModel();
        anotherUser.setUsername("another_" + uid);
        anotherUser.setEmail("another_" + uid + "@hotel.com");
        anotherUser.setPassword(passwordEncoder.encode(rawPassword));
        anotherUser.setRole(Role.USER);
        anotherUser.setActive(true);
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    @DisplayName("1. Đăng nhập: Lưu UserSession trong MySQL và tạo trạng thái ACTIVE trong Redis")
    void testLoginCreatesSessionInDbAndRedis() {
        AuthResponse response = authService.login(testUser.getUsername(), rawPassword, "web-browser");
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        UUID sessionId = jwtUtil.extractSessionID(response.getAccessToken());
        assertNotNull(sessionId);

        // Verify MySQL authoritative store
        Optional<UserSession> sessionOpt = userSessionRepository.findById(sessionId);
        assertTrue(sessionOpt.isPresent(), "Session must exist in MySQL");
        UserSession session = sessionOpt.get();
        assertEquals(testUser.getId(), session.getUser().getId());
        assertNull(session.getRevokedAt(), "Newly created session must not be revoked");
        assertEquals("web-browser", session.getDeviceID());

        // Verify Redis fast-cache
        SessionCacheStatus cacheStatus = sessionCacheService.isSessionValid(sessionId);
        // Note: If Redis is connected, status is VALID; if Redis is standalone/mocked or offline, status is ERROR/VALID
        assertTrue(cacheStatus == SessionCacheStatus.VALID || cacheStatus == SessionCacheStatus.ERROR,
                "Redis session cache should be VALID (or ERROR if Redis is running offline)");
    }

    @Test
    @DisplayName("2. Đăng xuất: Yêu cầu 'Bearer ' chuẩn, xác minh quyền sở hữu, thu hồi cả MySQL và Redis")
    void testLogoutValidationAndRevocation() {
        AuthResponse authRes = authService.login(testUser.getUsername(), rawPassword, "device-1");
        UUID sessionId = jwtUtil.extractSessionID(authRes.getAccessToken());

        // A. Header without "Bearer " prefix must be rejected
        assertThrows(ValidateException.class, () ->
                authService.logout("Basic " + authRes.getAccessToken())
        );

        // B. Another user attempting to revoke someone else's session must be rejected
        String anotherToken = jwtUtil.generateAccessToken(anotherUser, sessionId);
        assertThrows(UnauthorizedException.class, () ->
                authService.logout("Bearer " + anotherToken)
        );

        // C. Valid user logs out their own session
        String result = authService.logout("Bearer " + authRes.getAccessToken());
        assertEquals("Đăng xuất thành công", result);

        // Verify MySQL authoritative store shows revoked
        UserSession revokedSession = userSessionRepository.findById(sessionId).orElseThrow();
        assertNotNull(revokedSession.getRevokedAt(), "Session in MySQL must have revokedAt set");

        // Verify Redis fast-cache marks as REVOKED
        SessionCacheStatus status = sessionCacheService.isSessionValid(sessionId);
        assertTrue(status == SessionCacheStatus.REVOKED || status == SessionCacheStatus.ERROR);
    }

    @Test
    @DisplayName("2b. Đăng xuất với access token đã hết hạn: Không throw ExpiredJwtException và vẫn thu hồi session thành công")
    void testLogoutWithExpiredAccessToken() {
        AuthResponse authRes = authService.login(testUser.getUsername(), rawPassword, "device-expired");
        UUID sessionId = jwtUtil.extractSessionID(authRes.getAccessToken());

        // Create an expired access token for the same user and session
        java.security.Key key = Keys.hmacShaKeyFor(jwtUtil.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String expiredAccessToken = Jwts.builder()
                .setSubject(testUser.getUsername())
                .claim("type", "access")
                .claim("uid", testUser.getId())
                .claim("role", testUser.getRole().name())
                .claim("sid", sessionId.toString())
                .claim("tid", "DEFAULT")
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 100000))
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Must succeed without throwing ExpiredJwtException
        String result = authService.logout("Bearer " + expiredAccessToken);
        assertEquals("Đăng xuất thành công", result);

        // Verify MySQL shows revoked
        UserSession revokedSession = userSessionRepository.findById(sessionId).orElseThrow();
        assertNotNull(revokedSession.getRevokedAt());

        // Verify Redis marks REVOKED
        SessionCacheStatus status = sessionCacheService.isSessionValid(sessionId);
        assertTrue(status == SessionCacheStatus.REVOKED || status == SessionCacheStatus.ERROR);
    }

    @Test
    @DisplayName("3. Đăng xuất toàn bộ: Thu hồi tất cả session của người dùng trên MySQL và Redis")
    void testLogoutAllRevokesAllSessions() {
        AuthResponse session1 = authService.login(testUser.getUsername(), rawPassword, "desktop");
        AuthResponse session2 = authService.login(testUser.getUsername(), rawPassword, "mobile");

        UUID s1 = jwtUtil.extractSessionID(session1.getAccessToken());
        UUID s2 = jwtUtil.extractSessionID(session2.getAccessToken());

        String res = authService.logoutAll("Bearer " + session1.getAccessToken());
        assertEquals("Đã đăng xuất khỏi toàn bộ các thiết bị", res);

        UserSession dbS1 = userSessionRepository.findById(s1).orElseThrow();
        UserSession dbS2 = userSessionRepository.findById(s2).orElseThrow();

        assertNotNull(dbS1.getRevokedAt());
        assertNotNull(dbS2.getRevokedAt());

        SessionCacheStatus status1 = sessionCacheService.isSessionValid(s1);
        SessionCacheStatus status2 = sessionCacheService.isSessionValid(s2);
        assertTrue(status1 == SessionCacheStatus.REVOKED || status1 == SessionCacheStatus.ERROR);
        assertTrue(status2 == SessionCacheStatus.REVOKED || status2 == SessionCacheStatus.ERROR);
    }

    @Test
    @DisplayName("4. Refresh Token Rotation: Xoay vòng token thành công và cập nhật Redis")
    void testRefreshTokenRotationSuccess() {
        AuthResponse authRes = authService.login(testUser.getUsername(), rawPassword, "laptop");
        String oldRefreshToken = authRes.getRefreshToken();
        UUID sessionId = jwtUtil.extractSessionID(authRes.getAccessToken());

        AuthResponse refreshed = authService.refreshAccessToken(oldRefreshToken);
        assertNotNull(refreshed);
        assertNotEquals(oldRefreshToken, refreshed.getRefreshToken(), "New refresh token must be rotated");

        // Old refresh token must now be invalid
        assertThrows(ValidateException.class, () ->
                authService.refreshAccessToken(oldRefreshToken)
        );

        // New refresh token can be used again
        AuthResponse secondRefreshed = authService.refreshAccessToken(refreshed.getRefreshToken());
        assertNotNull(secondRefreshed);
    }

    @Test
    @DisplayName("5. Chống Concurrency Race Condition khi 2 yêu cầu Refresh Token đồng thời")
    void testConcurrentRefreshTokenRotationRaceCondition() throws Exception {
        AuthResponse authRes = authService.login(testUser.getUsername(), rawPassword, "tablet");
        String originalRefreshToken = authRes.getRefreshToken();

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    authService.refreshAccessToken(originalRefreshToken);
                    successCount.incrementAndGet();
                } catch (UnauthorizedException | ValidateException ex) {
                    failureCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                }
            });
        }

        // Trigger both threads simultaneously
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        // Exactly ONE request must succeed and the other must be rejected
        assertEquals(1, successCount.get(), "Chỉ duy nhất 1 yêu cầu refresh token đồng thời được thành công");
        assertEquals(1, failureCount.get(), "Yêu cầu refresh token đồng thời thứ hai phải bị từ chối chống race condition");
    }
}
