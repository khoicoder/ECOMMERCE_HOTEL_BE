//package com.example.BE.config;
//
//import com.example.BE.model.HotelModel;
//import com.example.BE.model.RoomModel;
//import com.example.BE.enums.RoomStatus;
//import com.example.BE.repository.HotelRepository;
//import com.example.BE.repository.RoomRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//public class DataInitializer {
//
//    @Bean
//    CommandLineRunner initDatabase(HotelRepository hotelRepository, RoomRepository roomRepository) {
//        return args -> {
//            System.out.println(">> Đang làm sạch dữ liệu cũ để tránh lỗi Enum 'available'...");
//
//            // 1. XÓA SẠCH TRƯỚC (Xóa Room trước vì dính Khóa ngoại tới bảng Hotels)
//            roomRepository.deleteAll();
//            hotelRepository.deleteAll();
//
//            System.out.println(">> Đang tạo dữ liệu Khách sạn mẫu theo đúng cấu trúc HotelModel...");
//
//            // 2. Tạo danh sách các khách sạn mẫu chứa đầy đủ thông tin (name, location, rating, priceFrom, image, description)
//            List<HotelModel> hotelTemplates = new ArrayList<>();
//            for (int i = 1; i <= 20; i++) {
//                HotelModel hotel = new HotelModel();
//                hotel.setName("Khách sạn Luxury " + i);
//                hotel.setLocation("Khu du lịch Trung tâm, Phường " + i +", TP. Đà Nẵng");
//                hotel.setRating(4.5);
//                hotel.setPriceFrom(350000.0);
//                hotel.setImage("https://images.unsplash.com/photo-hotel-" + i + ".jpg");
//                hotel.setDescription("Khách sạn tiêu chuẩn quốc tế đầy đủ tiện nghi, hồ bơi, buffet sáng miễn phí tại khu vực " + i);
//                hotel.setRooms(new ArrayList<>()); // Khởi tạo danh sách phòng trống ban đầu
//                hotelTemplates.add(hotel);
//            }
//            // Lưu xuống database để sinh tự động ID (1, 2, 3...) ổn định cho khóa ngoại
//            List<HotelModel> savedHotels = hotelRepository.saveAll(hotelTemplates);
//
//            System.out.println(">> Đang đồng bộ hóa dữ liệu Phòng mẫu dạng CHỮ HOA...");
//
//            // 3. Toàn bộ data phòng cũ từ CSV đã được chuẩn hóa CHỮ HOA toàn bộ ở cột Trạng thái (Cột số 4)
//            String[][] formattedRoomData = {
//                    {"Room 101", "Single", "350000", "AVAILABLE", "1"},
//                    {"Room 102", "Double", "500000", "BOOKED", "1"},
//                    {"Room 103", "VIP", "800000", "MAINTENANCE", "1"},
//                    {"Room 201", "Single", "500000", "AVAILABLE", "2"},
//                    {"Room 202", "Double", "700000", "BOOKED", "2"},
//                    {"Room 203", "VIP", "1200000", "AVAILABLE", "2"},
//                    {"Room 301", "Single", "450000", "AVAILABLE", "3"},
//                    {"Room 302", "Double", "650000", "AVAILABLE", "3"},
//                    {"Room 303", "VIP", "1000000", "BOOKED", "3"},
//                    {"Room 401", "Single", "400000", "AVAILABLE", "4"},
//                    {"Room 402", "Double", "600000", "BOOKED", "4"},
//                    {"Room 403", "VIP", "950000", "MAINTENANCE", "4"},
//                    {"Room 1301", "Single", "630000", "AVAILABLE", "13"},
//                    {"Room 1302", "Double", "830000", "BOOKED", "13"},
//                    {"Room 1303", "VIP", "1250000", "AVAILABLE", "13"},
//                    {"Room 1401", "Single", "390000", "AVAILABLE", "14"},
//                    {"Room 1402", "Double", "590000", "BOOKED", "14"},
//                    {"Room 1403", "VIP", "880000", "AVAILABLE", "14"},
//                    {"Room 1501", "Single", "360000", "AVAILABLE", "15"},
//                    {"Room 1502", "Double", "540000", "MAINTENANCE", "15"},
//                    {"Room 1503", "VIP", "820000", "AVAILABLE", "15"},
//                    {"Room 1601", "Single", "410000", "AVAILABLE", "16"},
//                    {"Room 1602", "Double", "610000", "BOOKED", "16"},
//                    {"Room 1603", "VIP", "910000", "AVAILABLE", "16"},
//                    {"Room 1701", "Single", "440000", "AVAILABLE", "17"},
//                    {"Room 1702", "Double", "640000", "AVAILABLE", "17"},
//                    {"Room 1703", "VIP", "990000", "AVAILABLE", "17"}
//            };
//
//            List<RoomModel> roomsToSave = new ArrayList<>();
//
//            for (String[] row : formattedRoomData) {
//                String name = row[0];
//                String type = row[1];
//                Double price = Double.parseDouble(row[2]);
//                String statusStr = row[3];
//                int hotelIdMapping = Integer.parseInt(row[4]); // ID của hotel từ file cũ
//
//                // Map trực tiếp vào Enum viết hoa (Không sợ bị lỗi No enum constant nữa)
//                RoomStatus statusEnum = RoomStatus.valueOf(statusStr);
//
//                // Tìm thực thể Hotel tương ứng dựa trên index của List đã save (trừ 1 vì index chạy từ 0)
//                int targetIndex = hotelIdMapping - 1;
//                HotelModel attachedHotel;
//                if (targetIndex >= 0 && targetIndex < savedHotels.size()) {
//                    attachedHotel = savedHotels.get(targetIndex);
//                } else {
//                    attachedHotel = savedHotels.get(0); // Trả về khách sạn đầu tiên làm phòng hờ
//                }
//
//                // Khởi tạo phòng mới dựa trên AllArgsConstructor của RoomModel
//                RoomModel room = new RoomModel(null, name, type, price, statusEnum, attachedHotel);
//                roomsToSave.add(room);
//
//                // Đồng bộ quan hệ hai chiều: Thêm phòng này vào danh sách phòng của Khách sạn đó
//                if (attachedHotel.getRooms() == null) {
//                    attachedHotel.setRooms(new ArrayList<>());
//                }
//                attachedHotel.getRooms().add(room);
//            }
//
//            // 4. Lưu toàn bộ danh sách phòng xuống DB
//            roomRepository.saveAll(roomsToSave);
//
//            System.out.println(">> [SUCCESS] ĐÃ NẠP MỚI TOÀN BỘ DỮ LIỆU SẠCH!");
//            System.out.println(">> Số lượng khách sạn hiện có: " + hotelRepository.count());
//            System.out.println(">> Số lượng phòng hiện có: " + roomRepository.count());
//        };
//    }
//}