-- ==============================================================================
-- DATABASE SEED SCRIPT: VIETNAM LUXURY & BOUTIQUE HOTEL MANAGEMENT SYSTEM
-- Purpose: Real domain data, Edge-Case testing, Multi-tenant Admin Scoping
-- ==============================================================================

USE hotel;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. USERS (Super Admin, Hotel Admins, Real Vietnamese Customers)
-- Password for all accounts: Admin123@ ($2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y)
DELETE FROM users;

-- Super Admin (Tổng điều hành toàn bộ chuỗi hệ thống khách sạn)
INSERT INTO users (id, username, email, password, phone, address, active, role, created_at, updated_at) VALUES
(1, 'admin', 'admin@vietnamhotels.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0909999999', 'Tòa nhà Landmark 81, Bình Thạnh, TP.HCM', 1, 'SUPER_ADMIN', NOW(), NOW());

-- Hotel Admins (Quản trị viên / Chủ khách sạn từng chi nhánh cụ thể)
INSERT INTO users (id, username, email, password, phone, address, active, role, created_at, updated_at) VALUES
(2, 'khoi', 'khoi.hotel@saigonluxury.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0908888888', '123 Lê Lợi, Bến Nghé, Quận 1, TP.HCM', 1, 'ADMIN', NOW(), NOW()),
(3, 'admin_danang', 'manager@danangoceanic.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0907777777', '45 Võ Nguyên Giáp, Ngũ Hành Sơn, Đà Nẵng', 1, 'ADMIN', NOW(), NOW()),
(4, 'admin_dalat', 'manager@pinemountaindalat.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0906666666', '12 Trần Hưng Đạo, Phường 10, TP. Đà Lạt', 1, 'ADMIN', NOW(), NOW()),
(5, 'admin_hanoi', 'manager@hanoipalace.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0905555555', '36 Hàng Gai, Hoàn Kiếm, Hà Nội', 1, 'ADMIN', NOW(), NOW()),
(6, 'admin_phuquoc', 'manager@phuquocresort.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0904444444', 'Bãi Trường, Dương Tơ, TP. Phú Quốc', 1, 'ADMIN', NOW(), NOW()),
(7, 'admin_sapa', 'manager@saparetreat.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0903333333', 'Thung Lũng Mường Hoa, Thị xã Sa Pa', 1, 'ADMIN', NOW(), NOW()),
(8, 'letan', 'letan@vietnamhotels.vn', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0901234567', 'Grand Luxury Hotel & Spa - TP.HCM', 1, 'RECEPTIONIST', NOW(), NOW());

-- Customers (Khách hàng thực tế phục vụ kiểm thử nghiệp vụ đặt phòng)
INSERT INTO users (id, username, email, password, phone, address, active, role, created_at, updated_at) VALUES
(10, 'khachhang_vip', 'tranminhanh@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0901234567', '48 Phố Huế, Hai Bà Trưng, Hà Nội', 1, 'USER', NOW(), NOW()),
(11, 'le_hoang_nam', 'nam.lehoang@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0912233445', '120 Lạch Tray, Ngô Quyền, Hải Phòng', 1, 'USER', NOW(), NOW()),
(12, 'tran_thuy_tien', 'thuytien.tran@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0905556677', '215 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', 1, 'USER', NOW(), NOW()),
(13, 'nguyen_hoang_quan', 'quan.nguyenhoang@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0903332211', '89 Đường 30/4, Ninh Kiều, Cần Thơ', 1, 'USER', NOW(), NOW()),
(14, 'pham_quoc_huy', 'huy.phamquoc@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0908887766', '12 Bạch Đằng, Hải Châu, Đà Nẵng', 1, 'USER', NOW(), NOW()),
(15, 'do_thu_trang', 'trang.dothu@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0904443322', '78 Trần Phú, Lộc Thọ, TP. Nha Trang', 1, 'USER', NOW(), NOW()),
(16, 'hoang_gia_bao', 'bao.hoanggia@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0906665544', '5 Phan Đình Phùng, Phường 2, TP. Đà Lạt', 1, 'USER', NOW(), NOW()),
(17, 'vu_thi_lan', 'lan.vuthi@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0918881122', '34 Hạ Long, TP. Hạ Long, Quảng Ninh', 1, 'USER', NOW(), NOW()),
(18, 'dinh_thi_ngoc', 'ngoc.dinhthi@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0937774455', '56 Thùy Vân, Phường Thắng Tam, Vũng Tàu', 1, 'USER', NOW(), NOW()),
(19, 'user_locked', 'user.locked@gmail.com', '$2a$10$ef4qalVyiXO7uJGYww/GjuzbC1cWTQ3T1PjpnbCwdcmCwijBXdc5y', '0903333333', '12 Nguyễn Huệ, TP. Huế', 0, 'USER', NOW(), NOW());

-- 2. HOTELS (6 Khách sạn tiêu chuẩn 4-5 sao trải dài khắp Việt Nam)
DELETE FROM hotels;

INSERT INTO hotels (hotel_id, name, location, rating, price_from, image, description, status, owner_id, created_at, updated_at) VALUES
(1, 'Grand Luxury Hotel & Spa - Sài Gòn', '123 Đường Lê Lợi, Bến Nghé, Quận 1, TP. Hồ Chí Minh', 4.9, 850000.00, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', 'Khách sạn 5 sao sang trọng ngay trung tâm Sài Gòn với view sông tuyệt đẹp, hồ bơi vô cực tầng thượng, phòng gym 24/7 và nhà hàng ẩm thực Michelin.', 'ACTIVE', 2, NOW(), NOW()),
(2, 'Oceanic Resort & Beach Villas - Mỹ Khê Đà Nẵng', '45 Võ Nguyên Giáp, Quận Ngũ Hành Sơn, Đà Nẵng', 4.8, 1200000.00, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80', 'Khu nghỉ dưỡng biển đẳng cấp bãi biển Mỹ Khê với biệt thự ngắm bình minh, bãi tắm riêng, spa trị liệu và nhà hàng hải sản cao cấp.', 'ACTIVE', 3, NOW(), NOW()),
(3, 'Pine Mountain Heritage Lodge - Đà Lạt', '12 Đường Trần Hưng Đạo, Phường 10, TP. Đà Lạt', 4.7, 700000.00, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80', 'Dinh thự phong cách Pháp cổ giữa rừng thông ngút ngàn Đà Lạt. Không gian tĩnh lặng, lò sưởi ấm cúng, trà chiều sân vườn và view thung lũng sương mù.', 'ACTIVE', 4, NOW(), NOW()),
(4, 'Hà Nội Old Quarter Palace & Suites', '36 Phố Hàng Gai, Quận Hoàn Kiếm, Hà Nội', 4.8, 950000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80', 'Khách sạn phong cách Indochine hoài niệm ngay giữa lòng 36 phố phường, chỉ 3 phút đi bộ ra Hồ Hoàn Kiếm. Tiện nghi cao cấp kết hợp nét văn hóa thủ đô.', 'ACTIVE', 5, NOW(), NOW()),
(5, 'Emerald Bay Beachfront Resort & Spa - Phú Quốc', 'Khu Bãi Trường, Xã Dương Tơ, TP. Phú Quốc', 4.9, 1500000.00, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80', 'Thiên đường nghỉ dưỡng biển đảo với cát trắng mịn và biển xanh ngọc bích. Biệt thự hồ bơi riêng, sunset bar ngắm hoàng hôn đẹp nhất đảo ngọc.', 'ACTIVE', 6, NOW(), NOW()),
(6, 'Sa Pa Misty Valley Mountain Retreat', 'Đường Mường Hoa, Thị xã Sa Pa, Lào Cai', 4.7, 800000.00, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80', 'Khu nghỉ dưỡng trên đỉnh đồi săn mây ngắm trọn thung lũng ruộng bậc thang Mường Hoa và dãy Hoàng Liên Sơn hùng vĩ. Trải nghiệm tắm lá thuốc Dao Đỏ.', 'ACTIVE', 7, NOW(), NOW());

-- 3. ROOMS (Cơ cấu phòng đa dạng, phục vụ kiểm thử trường hợp chuẩn và trường hợp biên)
DELETE FROM rooms;

-- === HOTEL 1: Grand Luxury Sài Gòn (12 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(101, 'Phòng 101 - Standard Single', 'Standard Single', 650000.00, 'AVAILABLE', 1, NOW(), NOW()),
(102, 'Phòng 102 - Standard Single', 'Standard Single', 650000.00, 'AVAILABLE', 1, NOW(), NOW()),
(103, 'Phòng 103 - Superior Double', 'Superior Double', 850000.00, 'OCCUPIED', 1, NOW(), NOW()),
(104, 'Phòng 104 - Superior Double', 'Superior Double', 850000.00, 'BOOKED', 1, NOW(), NOW()),
(105, 'Phòng 105 - Standard Twin', 'Standard Twin', 750000.00, 'OCCUPIED', 1, NOW(), NOW()),
(106, 'Phòng 106 - Standard Twin', 'Standard Twin', 750000.00, 'CLEANING', 1, NOW(), NOW()),
(201, 'Phòng 201 - Deluxe City View', 'Deluxe City View', 1100000.00, 'AVAILABLE', 1, NOW(), NOW()),
(202, 'Phòng 202 - Deluxe City View', 'Deluxe City View', 1100000.00, 'OCCUPIED', 1, NOW(), NOW()),
(203, 'Phòng 203 - Executive Suite', 'Executive Suite', 1600000.00, 'AVAILABLE', 1, NOW(), NOW()),
(204, 'Phòng 204 - Executive Suite', 'Executive Suite', 1600000.00, 'AVAILABLE', 1, NOW(), NOW()),
(205, 'Phòng 205 - Deluxe Family', 'Deluxe Family', 1450000.00, 'AVAILABLE', 1, NOW(), NOW()),
(206, 'Phòng 206 - Deluxe Family', 'Deluxe Family', 1450000.00, 'MAINTENANCE', 1, NOW(), NOW()),
(301, 'Phòng 301 - Presidential Suite', 'Presidential Suite', 3200000.00, 'AVAILABLE', 1, NOW(), NOW()),
(302, 'Phòng 302 - VIP Luxury Suite', 'VIP Luxury Suite', 2800000.00, 'AVAILABLE', 1, NOW(), NOW()),
(303, 'Phòng 303 - VIP Penthouse', 'VIP Penthouse', 4500000.00, 'OCCUPIED', 1, NOW(), NOW()),
(304, 'Phòng 304 - VIP Penthouse', 'VIP Penthouse', 4500000.00, 'OUT_OF_SERVICE', 1, NOW(), NOW());

-- === HOTEL 2: Oceanic Resort Đà Nẵng (8 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(2101, 'Phòng 101 - Garden View Villa', 'Garden View Villa', 1200000.00, 'AVAILABLE', 2, NOW(), NOW()),
(2102, 'Phòng 102 - Garden View Villa', 'Garden View Villa', 1200000.00, 'AVAILABLE', 2, NOW(), NOW()),
(2103, 'Phòng 103 - Pool Access Villa', 'Pool Access Villa', 1600000.00, 'OCCUPIED', 2, NOW(), NOW()),
(2104, 'Phòng 104 - Pool Access Villa', 'Pool Access Villa', 1600000.00, 'AVAILABLE', 2, NOW(), NOW()),
(2201, 'Phòng 201 - Oceanfront Suite', 'Oceanfront Suite', 2200000.00, 'AVAILABLE', 2, NOW(), NOW()),
(2202, 'Phòng 202 - Oceanfront Suite', 'Oceanfront Suite', 2200000.00, 'CLEANING', 2, NOW(), NOW()),
(2203, 'Phòng 203 - Beachfront Penthouse', 'Beachfront Penthouse', 3500000.00, 'MAINTENANCE', 2, NOW(), NOW()),
(2204, 'Phòng 204 - Beachfront Penthouse', 'Beachfront Penthouse', 3500000.00, 'OCCUPIED', 2, NOW(), NOW());

-- === HOTEL 3: Pine Mountain Đà Lạt (6 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(3101, 'Phòng 101 - Pine View Double', 'Pine View Double', 700000.00, 'AVAILABLE', 3, NOW(), NOW()),
(3102, 'Phòng 102 - Pine View Double', 'Pine View Double', 700000.00, 'OCCUPIED', 3, NOW(), NOW()),
(3103, 'Phòng 103 - Attic Wooden Room', 'Attic Wooden Room', 850000.00, 'AVAILABLE', 3, NOW(), NOW()),
(3104, 'Phòng 104 - Attic Wooden Room', 'Attic Wooden Room', 850000.00, 'AVAILABLE', 3, NOW(), NOW()),
(3201, 'Phòng 201 - Vintage Villa Master', 'Vintage Villa Master', 1350000.00, 'AVAILABLE', 3, NOW(), NOW()),
(3202, 'Phòng 202 - Vintage Villa Twin', 'Vintage Villa Twin', 1150000.00, 'CLEANING', 3, NOW(), NOW());

-- === HOTEL 4: Hà Nội Old Quarter Palace (6 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(4101, 'Phòng 101 - Old Quarter Superior', 'Old Quarter Superior', 950000.00, 'AVAILABLE', 4, NOW(), NOW()),
(4102, 'Phòng 102 - Old Quarter Superior', 'Old Quarter Superior', 950000.00, 'AVAILABLE', 4, NOW(), NOW()),
(4201, 'Phòng 201 - Hoan Kiem Balcony Deluxe', 'Hoan Kiem Balcony Deluxe', 1400000.00, 'OCCUPIED', 4, NOW(), NOW()),
(4202, 'Phòng 202 - Hoan Kiem Balcony Deluxe', 'Hoan Kiem Balcony Deluxe', 1400000.00, 'AVAILABLE', 4, NOW(), NOW()),
(4301, 'Phòng 301 - Indochine Heritage Suite', 'Indochine Heritage Suite', 2100000.00, 'OCCUPIED', 4, NOW(), NOW()),
(4302, 'Phòng 302 - Indochine Heritage Suite', 'Indochine Heritage Suite', 2100000.00, 'MAINTENANCE', 4, NOW(), NOW());

-- === HOTEL 5: Emerald Bay Phú Quốc (6 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(5101, 'Phòng 101 - Sunset Deluxe Pool Access', 'Sunset Deluxe Pool Access', 1500000.00, 'AVAILABLE', 5, NOW(), NOW()),
(5102, 'Phòng 102 - Sunset Deluxe Pool Access', 'Sunset Deluxe Pool Access', 1500000.00, 'OCCUPIED', 5, NOW(), NOW()),
(5201, 'Phòng 201 - Ocean Breeze Villa', 'Ocean Breeze Villa', 2800000.00, 'AVAILABLE', 5, NOW(), NOW()),
(5202, 'Phòng 202 - Ocean Breeze Villa', 'Ocean Breeze Villa', 2800000.00, 'BOOKED', 5, NOW(), NOW()),
(5301, 'Phòng 301 - Beachfront Infinity Villa', 'Beachfront Infinity Villa', 4800000.00, 'OCCUPIED', 5, NOW(), NOW()),
(5302, 'Phòng 302 - Beachfront Infinity Villa', 'Beachfront Infinity Villa', 4800000.00, 'AVAILABLE', 5, NOW(), NOW());

-- === HOTEL 6: Sa Pa Misty Valley Retreat (6 phòng) ===
INSERT INTO rooms (id, name, type, price, status, hotel_id, created_at, updated_at) VALUES
(6101, 'Phòng 101 - Mountain View Bungalow', 'Mountain View Bungalow', 800000.00, 'AVAILABLE', 6, NOW(), NOW()),
(6102, 'Phòng 102 - Mountain View Bungalow', 'Mountain View Bungalow', 800000.00, 'AVAILABLE', 6, NOW(), NOW()),
(6201, 'Phòng 201 - Cloud Valley Deluxe', 'Cloud Valley Deluxe', 1250000.00, 'OCCUPIED', 6, NOW(), NOW()),
(6202, 'Phòng 202 - Cloud Valley Deluxe', 'Cloud Valley Deluxe', 1250000.00, 'CLEANING', 6, NOW(), NOW()),
(6301, 'Phòng 301 - Fansipan Panorama Chalet', 'Fansipan Panorama Chalet', 1950000.00, 'AVAILABLE', 6, NOW(), NOW()),
(6302, 'Phòng 302 - Fansipan Panorama Chalet', 'Fansipan Panorama Chalet', 1950000.00, 'MAINTENANCE', 6, NOW(), NOW());

-- 4. BOOKINGS (Bao gồm các kịch bản: CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, PENDING)
DELETE FROM bookings;

-- === Hotel 1 Bookings (TP.HCM) ===
-- Booking 1001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1001, '2026-10-19', '2026-10-23', 3400000.00, 'CHECKED_IN', 103, 10, '2026-10-15 08:30:00', NOW());

-- Booking 1002: Đang giữ chỗ (CONFIRMED)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1002, '2026-10-25', '2026-10-28', 2550000.00, 'CONFIRMED', 104, 11, '2026-10-20 14:15:00', NOW());

-- Booking 1003: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1003, '2026-10-20', '2026-10-24', 3000000.00, 'CHECKED_IN', 105, 12, '2026-10-18 09:00:00', NOW());

-- Booking 1004: Đã hoàn tất (CHECKED_OUT - test doanh thu lịch sử)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1004, '2026-10-10', '2026-10-15', 5500000.00, 'CHECKED_OUT', 202, 13, '2026-10-05 16:40:00', NOW());

-- Booking 1005: VIP Penthouse (CONFIRMED)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1005, '2026-10-22', '2026-10-27', 22500000.00, 'CONFIRMED', 303, 10, '2026-10-19 11:20:00', NOW());

-- Booking 1006: Đã hủy (CANCELLED - Edge Case: KHÔNG được cộng vào doanh thu!)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(1006, '2026-10-15', '2026-10-18', 3300000.00, 'CANCELLED', 201, 14, '2026-10-12 10:00:00', NOW());

-- === Hotel 2 Bookings (Đà Nẵng) ===
-- Booking 2001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(2001, '2026-10-20', '2026-10-24', 6400000.00, 'CHECKED_IN', 2103, 14, '2026-10-17 11:20:00', NOW());

-- Booking 2002: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(2002, '2026-10-19', '2026-10-23', 14000000.00, 'CHECKED_IN', 2204, 15, '2026-10-16 18:00:00', NOW());

-- Booking 2003: Đã hủy (CANCELLED)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(2003, '2026-10-12', '2026-10-15', 6600000.00, 'CANCELLED', 2201, 11, '2026-10-08 14:00:00', NOW());

-- === Hotel 3 Bookings (Đà Lạt) ===
-- Booking 3001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(3001, '2026-10-20', '2026-10-23', 2100000.00, 'CHECKED_IN', 3102, 16, '2026-10-18 15:30:00', NOW());

-- Booking 3002: Đã hoàn tất (CHECKED_OUT)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(3002, '2026-10-08', '2026-10-11', 2550000.00, 'CHECKED_OUT', 3103, 12, '2026-10-01 10:15:00', NOW());

-- === Hotel 4 Bookings (Hà Nội) ===
-- Booking 4001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(4001, '2026-10-20', '2026-10-24', 5600000.00, 'CHECKED_IN', 4201, 13, '2026-10-18 09:30:00', NOW());

-- Booking 4002: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(4002, '2026-10-19', '2026-10-23', 8400000.00, 'CHECKED_IN', 4301, 17, '2026-10-16 14:45:00', NOW());

-- === Hotel 5 Bookings (Phú Quốc) ===
-- Booking 5001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(5001, '2026-10-18', '2026-10-22', 6000000.00, 'CHECKED_IN', 5102, 15, '2026-10-14 16:20:00', NOW());

-- Booking 5002: Đã giữ chỗ (CONFIRMED)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(5002, '2026-10-24', '2026-10-28', 11200000.00, 'CONFIRMED', 5202, 10, '2026-10-20 18:00:00', NOW());

-- Booking 5003: Infinity Villa (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(5003, '2026-10-20', '2026-10-25', 24000000.00, 'CHECKED_IN', 5301, 18, '2026-10-17 12:10:00', NOW());

-- === Hotel 6 Bookings (Sa Pa) ===
-- Booking 6001: Đang ở (CHECKED_IN)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(6001, '2026-10-20', '2026-10-23', 3750000.00, 'CHECKED_IN', 6201, 11, '2026-10-18 10:45:00', NOW());

-- Booking 6002: Đang chờ cọc (PENDING - Edge Case: Đang PENDING thì chưa tính doanh thu)
INSERT INTO bookings (id, check_in_date, check_out_date, total_price, booking_status, room_id, user_id, created_at, updated_at) VALUES
(6002, '2026-10-28', '2026-10-31', 5850000.00, 'PENDING', 6301, 16, '2026-10-21 08:00:00', NOW());

-- 5. EQUIPMENTS (Trang thiết bị khách sạn theo từng phòng)
DELETE FROM equipments;

INSERT INTO equipments (equipment_id, name, brand, serial_number, status, description, hotel_id, room_id, created_at, updated_at) VALUES
(101, 'Máy Lạnh Inverter Daikin 2HP FTKF50XVMV', 'Daikin', 'DK-98123-SG', 'WORKING', 'Làm mát nhanh, bảo dưỡng định kỳ hàng tháng', 1, 101, NOW(), NOW()),
(102, 'Smart Tivi Samsung 4K Crystal 55 inch AU7002', 'Samsung', 'SS-77123-SG', 'WORKING', 'Tivi thông minh kết nối Netflix, Youtube 4K', 1, 102, NOW(), NOW()),
(103, 'Hệ Thống Thẻ Từ Cửa Khóa Điện Tử Hafele EL9000', 'Hafele', 'LOCK-332-SG', 'UNDER_REPAIR', 'Kẹt chip đọc thẻ RFID, cần thay pin và nạp lại firmware', 1, 206, NOW(), NOW()),
(104, 'Máy Sấy Tóc Chuyên Dụng Panasonic 2000W', 'Panasonic', 'PA-5541-SG', 'WORKING', 'Máy sấy công suất cao, tạo ion bảo vệ tóc', 1, 104, NOW(), NOW()),
(201, 'Hệ Thống Nước Nóng Năng Lượng Mặt Trời Ariston 300L', 'Ariston', 'AR-SOLAR-DN', 'WORKING', 'Cung cấp nước nóng tự động 24/7 cho khu villa', 2, 2101, NOW(), NOW()),
(202, 'Điều Hòa Multi Daikin VRV Công Nghiệp', 'Daikin', 'DK-203B-DN', 'BROKEN', 'Hỏng cảm biến nhiệt gas lạnh, cần linh kiện thay thế', 2, 2203, NOW(), NOW()),
(301, 'Lò Sưởi Điện Giả Lập Than Hồng Kamin', 'Kamin', 'HEAT-881-DL', 'WORKING', 'Lò sưởi ấm cúng phong cách cổ điển Châu Âu', 3, 3101, NOW(), NOW()),
(302, 'Máy Pha Cà Phê Viên Nén Nespresso Citiz', 'Nespresso', 'NES-009-DL', 'REPLACED', 'Máy cũ đã hết khấu hao 3 năm, đã thay máy mới', 3, 3103, NOW(), NOW()),
(401, 'Hệ Thống Lọc Khí Khử Mùi Dyson Purifier Cool', 'Dyson', 'DYS-401-HN', 'WORKING', 'Lọc bụi mịn PM2.5 cho phòng phố cổ', 4, 4201, NOW(), NOW()),
(501, 'Tủ Lạnh Mini Bar Dometic Cửa Kính 40L', 'Dometic', 'DOM-501-PQ', 'WORKING', 'Tủ lạnh êm tuyệt đối không gây tiếng ồn ban đêm', 5, 5102, NOW(), NOW()),
(601, 'Bồn Tắm Gỗ Pơ Mu Ngâm Thuốc Dao Đỏ', 'Thủ Công', 'BAT-601-SP', 'WORKING', 'Bồn tắm gỗ thảo dược cao cấp hướng nhìn thung lũng', 6, 6201, NOW(), NOW());

-- 6. MAINTENANCE TICKETS (Phiếu bảo trì sửa chữa trang thiết bị)
DELETE FROM maintenance_tickets;

INSERT INTO maintenance_tickets (maintenance_id, title, description, hotel_name, maintenance_status, maintenance_type, hotel_id, room_id, equipment_id, assigned_to, created_by, created_at, updated_at) VALUES
(1, 'Khóa thẻ từ phòng 206 chập mạch', 'Đèn báo đỏ nhấp nháy liên tục, khách không quẹt thẻ mở cửa được', 'Grand Luxury Hotel & Spa - Sài Gòn', 'IN_PROGRESS', 'ROOM', 1, 206, 103, 2, 10, NOW(), NOW()),
(2, 'Điều hòa phòng 203 không lạnh, có tiếng kêu', 'Quạt dàn lạnh không quay, cảm biến nhiệt độ báo lỗi E3', 'Oceanic Resort & Beach Villas - Mỹ Khê Đà Nẵng', 'PENDING', 'EQUIPMENT', 2, 2203, 202, 3, 15, NOW(), NOW()),
(3, 'Bảo dưỡng định kỳ lò sưởi than hồng', 'Kiểm tra lau bụi thanh nhiệt và bóng Halogen trước mùa đông', 'Pine Mountain Heritage Lodge - Đà Lạt', 'COMPLETED', 'GENERAL', 3, 3101, 301, 4, 16, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
