# HƯỚNG DẪN VẬN HÀNH HỆ THỐNG GIÁM SÁT (OBSERVABILITY & MONITORING)

Tài liệu hướng dẫn triển khai, vận hành và khắc phục sự cố hệ thống giám sát toàn diện cho ứng dụng Java Spring Boot (Quản lý Khách sạn) sử dụng **Spring Boot Actuator**, **Micrometer**, **Prometheus** và **Grafana**.

---

## 1. TỔNG QUAN KIẾN TRÚC & DATA FLOW

Hệ thống Observability được thiết kế theo mô hình Pull-based Metrics:

```
+-----------------------------------------------------------------------------------------+
|                                      JVM BACKEND                                        |
|                                                                                         |
|  [Spring Boot WebMVC]     [HikariCP Pool]       [Redis Client]      [Logback Engine]    |
|            |                     |                     |                    |           |
|  http.server.requests     hikaricp.* metrics     redis connection   Total Size Cap      |
|            \                     |                     /                    |           |
|             \                    |                    /                     v           |
|              v                   v                   v             [LogStorageMetrics]  |
|          +---------------------------------------------------+              |           |
|          |                 MICROMETER REGISTRY               |<-------------+           |
|          |         (PrometheusMeterRegistry instance)        |<-------------+           |
|          +---------------------------------------------------+    [HotelBusinessMetrics]|
|                                  |                                                      |
|                                  v                                                      |
|                   GET /actuator/prometheus (HTTP Scrape)                                |
+----------------------------------|------------------------------------------------------+
                                   | (Mỗi 15s Pull)
                                   v
             +-------------------------------------------+
             |                 PROMETHEUS                |
             |     - Port 9090 (TSDB Storage Engine)     |
             |     - Rule Engine (alert.rules.yml)       |
             +-------------------------------------------+
                                   | (Query PromQL)
                                   v
             +-------------------------------------------+
             |                  GRAFANA                  |
             |     - Port 3001 (Web Dashboard UI)        |
             |     - Auto-provisioned Dashboards         |
             +-------------------------------------------+
```

### Data Flow:
1. **Thu thập dữ liệu trong ứng dụng:**
   - **Hệ thống (JVM, OS, GC):** Micrometer tự động kết nối với JVM MBeans để đọc Memory (Heap/Non-Heap), Threads, Garbage Collection pause, CPU load.
   - **Database Connection Pool:** HikariCP tự động đăng ký metrics vào Micrometer (`HotelHikariCP` pool name) ghi nhận active, idle, pending connections và thời gian chờ connection (`acquire_seconds`).
   - **HTTP Requests:** Bộ lọc tích hợp của Spring Boot đo lường tự động qua `http.server.requests` (status, method, uri template, percentiles p50, p95, p99 và histograms).
   - **Log Storage (`LogStorageMetrics`):** Định kỳ 30 giây (quét nền phi tập trung, không I/O blocking) tính dung lượng file trong thư mục logs, đối chiếu với cấu hình `logging.logback.rollingpolicy.total-size-cap: 1GB`.
   - **Business Operations (`HotelBusinessMetrics`):** Ghi nhận lượt đặt phòng (success/failed), check-in, check-out, và thanh toán với tag cố định có cardinality thấp.
2. **Prometheus Scraping:**
   - Prometheus server gửi HTTP GET định kỳ 15 giây tới endpoint `/actuator/prometheus`.
   - Dữ liệu được lưu trữ dạng chuỗi thời gian (TSDB) với retention mặc định 15 ngày.
3. **Grafana Visualization & Alerting:**
   - Tự động nạp DataSource Prometheus qua provisioning.
   - Hiển thị bảng điều khiển `Hotel Backend Observability Dashboard` theo thời gian thực.
   - Prometheus đánh giá các quy tắc cảnh báo trong `alert.rules.yml`.

---

## 2. DANH SÁCH DEPENDENCIES

Các thư viện đã bổ sung trong `build.gradle` (tương thích nguyên bản Spring Boot 4.0.5 & Java 21, quản lý qua Spring Boot Dependency BOM):

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

*Không phát sinh xung đột, không nâng hạ phiên bản framework.*

---

## 3. CẤU TRÚC FILE HỆ THỐNG GIÁM SÁT

```
BE/
├── build.gradle                                            # Bổ sung actuator & micrometer-registry-prometheus
├── docker-compose.monitoring.yml                           # Docker Compose cho Prometheus (9090) & Grafana (3001)
├── monitoring/
│   ├── .env.monitoring.example                             # Biến môi trường mẫu cho ports và mật khẩu Grafana
│   ├── prometheus/
│   │   ├── prometheus.yml                                  # Cấu hình scrape interval 15s & target host.docker.internal
│   │   └── alert.rules.yml                                 # Bộ quy tắc cảnh báo ngưỡng (5xx, latency, heap, pool, log)
│   └── grafana/
│       └── provisioning/
│           ├── datasources/
│           │   └── prometheus-datasource.yml               # Tự động nạp datasource Prometheus
│           └── dashboards/
│               ├── dashboard-provider.yml                  # Tự động nạp thư mục dashboard
│               └── hotel-backend-dashboard.json            # Dashboard JSON hoàn chỉnh 5 phân hệ
└── src/
    ├── main/
    │   ├── java/com/example/BE/
    │   │   ├── config/SecurityConfig.java                  # Bảo vệ Actuator: mở /actuator/health, /info, /prometheus; chặn các endpoint nhạy cảm
    │   │   ├── metrics/
    │   │   │   ├── LogStorageMetrics.java                  # Custom Component theo dõi dung lượng và cap của Logback
    │   │   │   └── HotelBusinessMetrics.java               # Custom Counter theo dõi booking, check-in, check-out, payment
    │   │   └── services/Booking/BookingServiceImpl.java    # Bổ sung metric hooks
    │   └── resources/
    │       └── application.yaml                            # Cấu hình Actuator, histogram percentiles, tags
    └── test/
        └── java/com/example/BE/metrics/
            ├── LogStorageMetricsTest.java                  # Unit test cho phân tích dung lượng, đơn vị, lỗi, rotation
            └── ActuatorAndMetricsIntegrationTest.java      # Integration test kiểm tra Actuator, Health và Prometheus scrape
```

---

## 4. CHI TIẾT CÁC CHỈ SỐ (METRICS CATALOG)

### A. Technical Metrics (Mặc định chuẩn hóa)
| Metric Name | Loại | Ý nghĩa vận hành |
|---|---|---|
| `http_server_requests_seconds_count` | Counter | Tổng số HTTP requests đã phục vụ |
| `http_server_requests_seconds_bucket` | Histogram | Phân bố thời gian phản hồi (dùng tính p50, p95, p99) |
| `jvm_memory_used_bytes{area="heap"}` | Gauge | Bộ nhớ Heap đang sử dụng thực tế (Bytes) |
| `jvm_memory_max_bytes{area="heap"}` | Gauge | Ngưỡng Heap tối đa được cấp cho JVM |
| `jvm_gc_pause_seconds_count` | Counter | Số lần Garbage Collection dừng ứng dụng |
| `process_cpu_usage` | Gauge | % CPU do tiến trình Backend Spring Boot tiêu thụ (0.0 - 1.0) |
| `jvm_threads_live_threads` | Gauge | Số lượng Java threads đang hoạt động |
| `hikaricp_connections_active` | Gauge | Số database connection đang xử lý truy vấn |
| `hikaricp_connections_idle` | Gauge | Số database connection đang rảnh trong pool |
| `hikaricp_connections_pending` | Gauge | Số threads đang bị nghẽn chờ xin connection |
| `hikaricp_connections_acquire_seconds` | Summary | Thời gian chờ để lấy connection từ pool |

### B. Custom Application Metrics
| Metric Name | Loại | Ý nghĩa vận hành |
|---|---|---|
| `app.log.storage.used.bytes` | Gauge | Tổng dung lượng thực tế của thư mục log `logs/` (Bytes) |
| `app.log.storage.cap.bytes` | Gauge | Ngưỡng giới hạn cấu hình `total-size-cap` (1GB = 1,073,741,824 Bytes) |
| `app.log.storage.usage.ratio` | Gauge | Tỷ lệ dung lượng log đã chiếm so với giới hạn cap (0.0 đến 1.0) |
| `hotel.bookings.total{status="SUCCESS"}` | Counter | Số lượng đơn đặt phòng tạo thành công |
| `hotel.bookings.total{status="FAILED"}` | Counter | Số lượng đơn đặt phòng thất bại |
| `hotel.guest.checkins.total` | Counter | Số lượt khách Check-in vào khách sạn |
| `hotel.guest.checkouts.total` | Counter | Số lượt khách Check-out trả phòng |
| `hotel.payments.processed.total{result="SUCCESS"}` | Counter | Số giao dịch thanh toán thành công |

---

## 5. PROMQL QUERIES THÔNG DỤNG

### Tỷ lệ lỗi HTTP 5xx (% trên tổng request):
```promql
(sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) or vector(0)) / (sum(rate(http_server_requests_seconds_count[1m])) > 0) * 100
```

### Độ trễ P95 (95% người dùng phản hồi dưới mốc này):
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))
```

### Tỷ lệ sử dụng bộ nhớ Heap (%):
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

### Tỷ lệ chiếm dụng kết nối Database HikariCP (%):
```promql
(hikaricp_connections_active{pool="HotelHikariCP"} / hikaricp_connections_max{pool="HotelHikariCP"}) * 100
```

### Tỷ lệ dung lượng Log so với Cap (%):
```promql
app_log_storage_usage_ratio * 100
```

---

## 6. HƯỚNG DẪN KHỞI CHẠY VÀ VẬN HÀNH

### Bước 1: Khởi động Backend Spring Boot
Chạy ứng dụng từ terminal máy chủ/máy trạm (port 8080):
```powershell
cd d:\Workspace\BE\BE
./gradlew bootRun
```
*Kiểm tra Actuator:*
- Health check: `http://localhost:8080/actuator/health` -> Trả về `{"status":"UP"}`
- Prometheus endpoint: `http://localhost:8080/actuator/prometheus` -> Xuất metrics text format

### Bước 2: Khởi động Cụm Monitoring (Prometheus & Grafana)
Sử dụng Docker Compose dành riêng cho monitoring:
```powershell
cd d:\Workspace\BE\BE
docker compose -f docker-compose.monitoring.yml up -d
```

*Kiểm tra trạng thái container:*
```powershell
docker compose -f docker-compose.monitoring.yml ps
```
Cả hai container `hotel-prometheus` và `hotel-grafana` phải ở trạng thái `healthy` hoặc `running`.

### Bước 3: Truy cập Prometheus Web UI
- Đường dẫn: `http://localhost:9090`
- Menu **Status -> Targets**: Target `spring-boot-backend` (`host.docker.internal:8080`) phải hiển thị **State: UP** (màu xanh lá).

### Bước 4: Truy cập Grafana Dashboard
- Đường dẫn: `http://localhost:3001` *(chọn port 3001 để không xung đột với React Frontend cổng 3000)*
- Đăng nhập:
  - Username: `admin`
  - Password: `admin123` *(hoặc cấu hình tùy biến qua file .env)*
- Vào Menu **Dashboards -> Hotel Backend Observability**:
  - Toàn bộ các đồ thị CPU, Heap, Threads, HikariCP, HTTP Latency, Log Storage và Business Counters đã được tự động liên kết và cập nhật dữ liệu.

---

## 7. QUY TẮC CẢNH BÁO (ALERTING RULES)

Các quy tắc được định nghĩa trong `monitoring/prometheus/alert.rules.yml`:

| Tên Cảnh Báo | Điều kiện kích hoạt | Mức độ | Hành động cần xử lý |
|---|---|---|---|
| `BackendServiceDown` | `up == 0` kéo dài > 1m | CRITICAL | Kiểm tra process backend bị crash, OOM-kill hoặc mất mạng |
| `HighHttp5xxRate` | Lỗi 5xx > 5% kéo dài > 2m | CRITICAL | Kiểm tra log exception tại `logs/application.log`, lỗi DB hoặc lỗi code |
| `HighHttpLatencyP95` | P95 latency > 1.5s kéo dài > 3m | WARNING | Rà soát truy vấn SQL chậm, full table scan hoặc deadlock |
| `HighJvmHeapUsage` | Heap > 85% kéo dài > 3m | WARNING | Kiểm tra memory leak, tăng Xmx hoặc tối ưu caching |
| `HikariPoolExhaustion`| Active connections > 85% max pool | WARNING | Kiểm tra transaction treo, connection leak thiếu close |
| `HikariPendingConnections`| Pending waiting > 3 kéo dài > 1m | CRITICAL | Connection pool bị nghẽn, cần nâng pool-size hoặc tối ưu query time |
| `LogStorageNearCap` | Dung lượng log > 90% cap | WARNING | Logback sắp kích hoạt xóa log cũ, cần archive log ra ngoài |

---

## 8. HƯỚNG DẪN XỬ LÝ SỰ CỐ (TROUBLESHOOTING)

1. **Prometheus Target hiển thị DOWN (`connection refused` hoặc `no route to host`):**
   - **Nguyên nhân:** Container Prometheus không kết nối được tới localhost của máy Windows.
   - **Xử lý:** Kiểm tra `extra_hosts: ["host.docker.internal:host-gateway"]` trong `docker-compose.monitoring.yml`. Đảm bảo tường lửa Windows cho phép Docker kết nối cổng 8080.
2. **Endpoint `/actuator/prometheus` trả về 403 Forbidden:**
   - **Nguyên nhân:** Spring Security chặn request do thiếu cấu hình permit.
   - **Xử lý:** Xác nhận trong `SecurityConfig.java` có quy tắc `.requestMatchers("/actuator/prometheus").permitAll()`.
3. **Log Storage Metrics hiển thị 0 Bytes:**
   - **Nguyên nhân:** Ứng dụng mới khởi chạy và chưa sinh file log trong thư mục `logs/`.
   - **Xử lý:** Gọi vài request API để ứng dụng ghi log vào `logs/application.log`, metric sẽ tự động cập nhật trong chu kỳ quét 30s kế tiếp.
4. **Grafana không hiển thị dữ liệu (No Data):**
   - **Xử lý:** Vào **Administration -> Data Sources -> Prometheus** bấm **Save & Test**. Đảm bảo URL kết nối giữa container là `http://prometheus:9090`.

---

## 9. GIỚI HẠN HIỆN TẠI & HƯỚNG PHÁT TRIỂN

### Giới hạn hiện tại:
- Hệ thống tập trung vào Metrics (State, Rate, Latency, Error, Saturation). Chưa tích hợp Alertmanager để gửi webhook trực tiếp tới Telegram/Slack.
- Phân hệ Log Monitoring hiện tại theo dõi ở mức dung lượng lưu trữ trên phân vùng đĩa (Storage Metrics), chưa lập chỉ mục nội dung log từng dòng (Log Aggregation).

### Hướng phát triển tiếp theo (khi có nhu cầu chứng minh):
- Tích hợp **Prometheus Alertmanager** cấu hình gửi cảnh báo tự động về kênh Telegram/Slack SRE.
- Bổ sung **Loki + Promtail** nếu dự án mở rộng lên quy mô phân tán nhiều cụm máy chủ và cần tìm kiếm log tập trung.
- Mở rộng Multi-tenant Metrics (phân đoạn lưu lượng request theo tenant code `DEFAULT`, `TENANT_HCM`,...).
