package com.example.BE.services.Booking;

import com.example.BE.dto.user.request.BookingRequest;
import com.example.BE.dto.user.request.CheckOutRequest;
import com.example.BE.dto.user.request.ProcessPaymentRequest;
import com.example.BE.dto.user.response.BillPreviewResponse;
import com.example.BE.dto.user.response.BookingResponse;
import com.example.BE.enums.*;
import com.example.BE.exception.BadRequestException;
import com.example.BE.exception.ConflictException;
import com.example.BE.exception.NotFoundException;
import com.example.BE.exception.UnauthorizedException;
import com.example.BE.model.*;
import com.example.BE.repository.*;
import com.example.BE.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final HotelStaffRepository hotelStaffRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public List<BookingResponse> findAllBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToResponseBooking).toList();
        }

        Role role = principal.role();
        if (role == Role.SUPER_ADMIN) {
            return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::mapToResponseBooking).toList();
        } else if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            HotelModel owned = findOwnedHotel(principal.userId());
            if (owned == null) {
                return bookingRepository.findByRoom_Hotel_IdOrderByCreatedAtDesc(1L).stream()
                        .map(this::mapToResponseBooking).toList();
            }
            return bookingRepository.findByRoom_Hotel_IdOrderByCreatedAtDesc(owned.getId()).stream()
                    .map(this::mapToResponseBooking).toList();
        } else {
            return bookingRepository.findByUserIdOrderByCreatedAtDesc(principal.userId()).stream()
                    .map(this::mapToResponseBooking).toList();
        }
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public BookingResponse createBooking(BookingRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal authPrincipal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập để thực hiện đặt phòng");
        }

        UserModel user = userRepository.findById(authPrincipal.userId()).orElseThrow(
                () -> new NotFoundException("Không tìm thấy thông tin tài khoản người dùng: " + authPrincipal.userId())
        );

        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Ngày nhận phòng và ngày trả phòng không được để trống");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Ngày nhận phòng không thể ở trong quá khứ (" + checkIn + ")");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Ngày trả phòng (" + checkOut + ") phải sau ngày nhận phòng (" + checkIn + ") ít nhất 1 đêm");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > 30) {
            throw new BadRequestException("Thời gian lưu trú tối đa cho phép là 30 đêm");
        }

        if (bookingRepository.existsDuplicateBooking(user.getId(), request.roomId(), checkIn, checkOut)) {
            throw new ConflictException("Bạn đã có một đơn đặt phòng trùng khớp đang chờ xử lý cho phòng này");
        }

        RoomModel room = roomRepository.findByIdWithLock(request.roomId()).orElseThrow(
                () -> new NotFoundException("Không tìm thấy thông tin phòng với ID: " + request.roomId())
        );

        if (room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Phòng '" + room.getName() + "' hiện đang bảo trì hoặc ngừng phục vụ, không thể đặt phòng");
        }

        boolean hasOverlap = bookingRepository.existsOverlappingBooking(room.getId(), checkIn, checkOut);
        if (hasOverlap) {
            throw new ConflictException("Phòng '" + room.getName() + "' đã có khách đặt trong khoảng thời gian từ "
                    + checkIn + " đến " + checkOut + ". Vui lòng chọn ngày khác hoặc phòng khác.");
        }

        BigDecimal roomPrice = room.getPrice() != null ? room.getPrice() : BigDecimal.valueOf(500000);
        BigDecimal totalPrice = roomPrice.multiply(BigDecimal.valueOf(nights));

        Instant now = Instant.now();
        Instant deadline = checkIn.equals(LocalDate.now())
                ? now.plus(2, ChronoUnit.HOURS)
                : now.plus(24, ChronoUnit.HOURS);

        BookingModel booking = new BookingModel();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setTotalPrice(totalPrice);
        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setRemainingAmount(totalPrice);
        booking.setExtraCharges(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setStayStatus(StayStatus.NOT_CHECKED_IN);
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setPaymentDeadline(deadline);

        if (checkIn.equals(LocalDate.now()) && room.getStatus() == RoomStatus.AVAILABLE) {
            room.setStatus(RoomStatus.BOOKED);
            roomRepository.save(room);
        }

        BookingModel savedBooking = bookingRepository.save(booking);
        logAudit(authPrincipal.userId(), authPrincipal.username(), "CREATE_BOOKING", "Tạo đơn đặt phòng #" + savedBooking.getId(), "BookingModel", savedBooking.getId());
        return mapToResponseBooking(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse checkIn(Long id, Authentication authentication) {
        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "thực hiện Check-in");

        // 1. Kiểm tra trạng thái đơn đặt phòng
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Không thể Check-in đơn đặt phòng đã bị hủy");
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Đơn đặt phòng đã hết hạn thanh toán, không thể Check-in");
        }
        if (booking.getStatus() == BookingStatus.NO_SHOW) {
            throw new BadRequestException("Đơn đặt phòng đã bị đánh dấu vắng mặt (No-Show), không thể Check-in");
        }

        // 2. Kiểm tra trạng thái lưu trú
        if (booking.getStayStatus() == StayStatus.CHECKED_IN) {
            throw new ConflictException("Đơn đặt phòng #" + id + " đã được Check-in trước đó (Khách đang lưu trú)");
        }
        if (booking.getStayStatus() == StayStatus.CHECKED_OUT) {
            throw new BadRequestException("Lượt lưu trú đã kết thúc (Checked-out), không thể Check-in lại");
        }

        // 3. Kiểm tra ngày nhận phòng
        LocalDate today = LocalDate.now();
        if (today.isAfter(booking.getCheckOutDate())) {
            throw new BadRequestException("Đã quá ngày trả phòng dự kiến (" + booking.getCheckOutDate() + "). Không thể Check-in");
        }

        // 4. Khóa độc quyền phòng và kiểm tra trạng thái vật lý
        RoomModel room = roomRepository.findByIdWithLock(booking.getRoom().getId()).orElseThrow(
                () -> new NotFoundException("Phòng gắn với đơn đặt không tồn tại")
        );

        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new ConflictException("Phòng " + room.getName() + " hiện đang có khách lưu trú khác");
        }
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new BadRequestException("Phòng " + room.getName() + " đang bảo trì kỹ thuật, không thể bàn giao khách");
        }
        if (room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Phòng " + room.getName() + " đã dừng phục vụ");
        }

        if (bookingRepository.hasActiveCheckedInStayOnRoom(room.getId(), id)) {
            throw new ConflictException("Phòng " + room.getName() + " đang có lượt lưu trú khác chưa Check-out");
        }

        // 5. Cập nhật trạng thái
        String actor = extractUsername(authentication);
        Instant now = Instant.now();

        booking.setStayStatus(StayStatus.CHECKED_IN);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setActualCheckInTime(now);
        booking.setCheckedInBy(actor);
        booking.setUpdatedAt(now);

        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);

        BookingModel saved = bookingRepository.save(booking);
        logAudit(extractUserId(authentication), actor, "CHECK_IN", "Khách Check-in phòng " + room.getName() + " đơn #" + id, "BookingModel", id);
        return mapToResponseBooking(saved);
    }

    @Override
    @Transactional
    public BookingResponse checkOut(Long id, CheckOutRequest request, Authentication authentication) {
        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "thực hiện Check-out");

        if (booking.getStayStatus() != StayStatus.CHECKED_IN) {
            throw new BadRequestException("Chỉ có thể Check-out cho đơn đặt phòng đang ở trạng thái CHECKED_IN (Hiện tại: "
                    + (booking.getStayStatus() != null ? booking.getStayStatus() : booking.getStatus()) + ")");
        }

        // Phụ thu dịch vụ phát sinh (nếu có)
        if (request != null && request.getExtraCharges() != null) {
            if (request.getExtraCharges().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Phí dịch vụ phát sinh không thể là số âm");
            }
            booking.setExtraCharges(booking.getExtraCharges() != null
                    ? booking.getExtraCharges().add(request.getExtraCharges())
                    : request.getExtraCharges());
        }

        BigDecimal totalBill = booking.getTotalPrice().add(booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO);
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remaining = totalBill.subtract(paid);
        booking.setRemainingAmount(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);

        String actor = extractUsername(authentication);
        Instant now = Instant.now();

        booking.setStayStatus(StayStatus.CHECKED_OUT);
        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setActualCheckOutTime(now);
        booking.setCheckedOutBy(actor);
        booking.setUpdatedAt(now);

        // Chuyển phòng sang CLEANING (chờ nhân viên buồng phòng dọn dẹp)
        RoomModel room = booking.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
        }

        BookingModel saved = bookingRepository.save(booking);
        logAudit(extractUserId(authentication), actor, "CHECK_OUT", "Khách Check-out đơn #" + id + ", dư nợ: " + remaining + " đ", "BookingModel", id);
        return mapToResponseBooking(saved);
    }

    @Override
    @Transactional
    public BookingResponse processReceptionistPayment(Long id, ProcessPaymentRequest request, Authentication authentication) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền thanh toán phải lớn hơn 0");
        }

        PaymentMethod method = request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH;

        if (method == PaymentMethod.CASH && !Boolean.TRUE.equals(request.getCashConfirmed())) {
            throw new BadRequestException("Vui lòng xác nhận lễ tân đã nhận đủ tiền mặt từ khách");
        }

        if ((method == PaymentMethod.BANK_TRANSFER || method == PaymentMethod.CREDIT_CARD)
                && (request.getTransactionCode() == null || request.getTransactionCode().isBlank())) {
            throw new BadRequestException("Phương thức " + method + " yêu cầu nhập mã bút toán / mã giao dịch tham chiếu");
        }

        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "xác nhận thanh toán");

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Không thể thu tiền cho đơn đặt phòng đã bị hủy");
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Đơn đặt phòng đã hết hạn thanh toán, không thể thu tiền");
        }

        BigDecimal extra = booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO;
        BigDecimal totalBill = booking.getTotalPrice().add(extra);
        BigDecimal currentPaid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal currentRemaining = totalBill.subtract(currentPaid);

        if (request.getAmount().compareTo(currentRemaining) > 0) {
            throw new BadRequestException("Số tiền thanh toán (" + request.getAmount()
                    + " đ) vượt quá số tiền còn nợ (" + currentRemaining + " đ)");
        }

        String txnCode = (request.getTransactionCode() != null && !request.getTransactionCode().isBlank())
                ? request.getTransactionCode().trim()
                : "CASH-" + booking.getId() + "-" + System.currentTimeMillis();

        if (paymentRepository.existsByTransactionCode(txnCode)) {
            throw new ConflictException("Mã giao dịch '" + txnCode + "' đã tồn tại trong hệ thống");
        }

        // Lưu bản ghi giao dịch thanh toán
        PaymentModel payment = new PaymentModel();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentProvider(method == PaymentMethod.CASH ? PaymentProvider.KHAC : PaymentProvider.VIETQR);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionCode(txnCode);
        payment.setReferenceNote(request.getReferenceNote());
        payment.setCashConfirmed(request.getCashConfirmed());
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedBy(extractUserId(authentication));
        paymentRepository.save(payment);

        // Cập nhật công nợ trên Booking
        BigDecimal newPaid = currentPaid.add(request.getAmount());
        BigDecimal newRemaining = totalBill.subtract(newPaid);

        booking.setPaidAmount(newPaid);
        booking.setRemainingAmount(newRemaining.compareTo(BigDecimal.ZERO) > 0 ? newRemaining : BigDecimal.ZERO);

        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            booking.setPaymentStatus(PaymentStatus.PAID);
        } else {
            booking.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        booking.setUpdatedAt(Instant.now());
        BookingModel saved = bookingRepository.save(booking);

        logAudit(extractUserId(authentication), extractUsername(authentication), "PAYMENT",
                "Thu tiền " + request.getAmount() + " đ qua " + method + " cho đơn #" + id, "PaymentModel", payment.getId());

        return mapToResponseBooking(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillPreviewResponse getBillPreview(Long id, Authentication authentication) {
        BookingModel booking = bookingRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "xem chi tiết hóa đơn");

        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (nights <= 0) nights = 1;

        BigDecimal pricePerNight = booking.getRoom() != null && booking.getRoom().getPrice() != null
                ? booking.getRoom().getPrice()
                : booking.getTotalPrice().divide(BigDecimal.valueOf(nights), BigDecimal.ROUND_HALF_UP);

        BigDecimal extra = booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO;
        BigDecimal totalBill = booking.getTotalPrice().add(extra);
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remaining = totalBill.subtract(paid);

        return BillPreviewResponse.builder()
                .bookingId(booking.getId())
                .guestName(booking.getUser() != null ? booking.getUser().getUsername() : "Khách")
                .roomNumber(booking.getRoom() != null ? booking.getRoom().getName() : "")
                .roomType(booking.getRoom() != null ? booking.getRoom().getType() : "")
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .actualNights(nights)
                .roomRatePerNight(pricePerNight)
                .totalRoomCharge(booking.getTotalPrice())
                .extraCharges(extra)
                .totalBill(totalBill)
                .paidAmount(paid)
                .remainingAmount(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO)
                .fullyPaid(remaining.compareTo(BigDecimal.ZERO) <= 0)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, Authentication authentication) {
        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Đơn đặt phòng này đã bị hủy trước đó");
        }

        if (booking.getStayStatus() == StayStatus.CHECKED_IN || booking.getStayStatus() == StayStatus.CHECKED_OUT) {
            throw new BadRequestException("Không thể hủy đơn đặt phòng đang trong kỳ lưu trú hoặc đã trả phòng");
        }

        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            Role role = principal.role();
            if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
                HotelModel owned = findOwnedHotel(principal.userId());
                Long hotelId = owned != null ? owned.getId() : 1L;
                if (booking.getRoom() == null || booking.getRoom().getHotel() == null
                        || !booking.getRoom().getHotel().getId().equals(hotelId)) {
                    throw new UnauthorizedException("Bạn chỉ có quyền hủy đơn đặt phòng thuộc khách sạn do bạn quản lý");
                }
            } else if (role == Role.USER) {
                if (booking.getUser() == null || !booking.getUser().getId().equals(principal.userId())) {
                    throw new UnauthorizedException("Bạn chỉ có quyền hủy đơn đặt phòng của chính bạn");
                }
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());

        RoomModel room = booking.getRoom();
        if (room != null && room.getStatus() == RoomStatus.BOOKED) {
            LocalDate today = LocalDate.now();
            boolean hasOther = bookingRepository.existsOverlappingBooking(room.getId(), today, today.plusDays(1));
            if (!hasOther) {
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }
        }

        return mapToResponseBooking(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public int expireOverdueBookings() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        List<BookingModel> overdues = bookingRepository.findOverduePendingBookings(now, today);
        int count = 0;

        for (BookingModel b : overdues) {
            // Không được hủy nếu đã check in hoặc đã thanh toán
            if (b.getStayStatus() == StayStatus.CHECKED_IN || b.getPaymentStatus() == PaymentStatus.PAID) {
                continue;
            }

            b.setStatus(BookingStatus.EXPIRED);
            b.setUpdatedAt(now);

            RoomModel room = b.getRoom();
            if (room != null && room.getStatus() == RoomStatus.BOOKED) {
                boolean hasOther = bookingRepository.existsOverlappingBooking(room.getId(), today, today.plusDays(1));
                if (!hasOther) {
                    room.setStatus(RoomStatus.AVAILABLE);
                    roomRepository.save(room);
                }
            }
            bookingRepository.save(b);
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public int markNoShowBookings() {
        LocalDate today = LocalDate.now();
        List<BookingModel> noShows = bookingRepository.findNoShowBookings(today);
        int count = 0;

        for (BookingModel b : noShows) {
            if (b.getStayStatus() == StayStatus.NOT_CHECKED_IN) {
                b.setStatus(BookingStatus.NO_SHOW);
                b.setUpdatedAt(Instant.now());

                RoomModel room = b.getRoom();
                if (room != null && room.getStatus() == RoomStatus.BOOKED) {
                    boolean hasOther = bookingRepository.existsOverlappingBooking(room.getId(), today, today.plusDays(1));
                    if (!hasOther) {
                        room.setStatus(RoomStatus.AVAILABLE);
                        roomRepository.save(room);
                    }
                }
                bookingRepository.save(b);
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatus newStatus, Authentication authentication) {
        if (newStatus == BookingStatus.CHECKED_IN) {
            return checkIn(id, authentication);
        } else if (newStatus == BookingStatus.CHECKED_OUT) {
            return checkOut(id, new CheckOutRequest(BigDecimal.ZERO, "Check-out nhanh"), authentication);
        } else if (newStatus == BookingStatus.CANCELLED) {
            return cancelBooking(id, authentication);
        }

        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "cập nhật trạng thái");

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Không thể thay đổi trạng thái của đơn đặt phòng đã hủy hoặc hết hạn");
        }

        booking.setStatus(newStatus);
        booking.setUpdatedAt(Instant.now());
        return mapToResponseBooking(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse updatePaymentStatus(Long id, String paymentStatusStr, Authentication authentication) {
        PaymentStatus newStatus = PaymentStatus.fromString(paymentStatusStr);
        BookingModel booking = bookingRepository.findByIdWithLock(id).orElseThrow(
                () -> new NotFoundException("Không tìm thấy đơn đặt phòng với ID: " + id)
        );

        verifyStaffHotelPermission(booking, authentication, "cập nhật thanh toán");

        if (newStatus == PaymentStatus.PAID) {
            BigDecimal extra = booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO;
            BigDecimal totalBill = booking.getTotalPrice().add(extra);
            BigDecimal currentPaid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal diff = totalBill.subtract(currentPaid);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                ProcessPaymentRequest req = new ProcessPaymentRequest(diff, PaymentMethod.CASH, null, "Xác nhận đã thanh toán tại quầy", true);
                return processReceptionistPayment(id, req, authentication);
            }
        } else if (newStatus == PaymentStatus.UNPAID) {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
            booking.setPaidAmount(BigDecimal.ZERO);
            booking.setRemainingAmount(booking.getTotalPrice().add(booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO));
            booking.setUpdatedAt(Instant.now());
            return mapToResponseBooking(bookingRepository.save(booking));
        }

        booking.setPaymentStatus(newStatus);
        booking.setUpdatedAt(Instant.now());
        return mapToResponseBooking(bookingRepository.save(booking));
    }

    @Override
    public List<BookingModel> findBookingByStatus(BookingStatus status) {
        return List.of();
    }

    @Override
    public List<BookingModel> findBookingByHotelId(Long hotelId) {
        return bookingRepository.findByRoom_Hotel_IdOrderByCreatedAtDesc(hotelId);
    }

    @Override
    public BookingModel findBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new NotFoundException("Booking not found: " + id));
    }

    @Override
    public BookingModel findBookingByEmail(String email) {
        return null;
    }

    @Override
    public BookingModel findById(Long id) {
        return findBookingById(id);
    }

    @Override
    public List<BookingResponse> findAllByUserId(Long id) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(id).stream()
                .map(this::mapToResponseBooking).toList();
    }

    @Override
    public void deleteBooking(BookingModel booking) {
        bookingRepository.delete(booking);
    }

    @Override
    public BookingResponse updateBooking(BookingRequest request, Authentication authentication) {
        return null;
    }

    private HotelModel findOwnedHotel(Long userId) {
        if (userId == null) return null;
        var opt = hotelRepository.findFirstByOwner_Id(userId);
        if (opt.isPresent()) return opt.get();
        var staffOpt = hotelStaffRepository.findFirstByUserId(userId);
        return staffOpt.map(com.example.BE.model.HotelStaffModel::getHotel).orElse(null);
    }

    private void verifyStaffHotelPermission(BookingModel booking, Authentication authentication, String actionName) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new UnauthorizedException("Vui lòng đăng nhập để " + actionName);
        }
        Role role = principal.role();
        if (role == Role.SUPER_ADMIN) {
            return;
        }
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            HotelModel owned = findOwnedHotel(principal.userId());
            Long assignedHotelId = owned != null ? owned.getId() : 1L;
            if (booking.getRoom() == null || booking.getRoom().getHotel() == null
                    || !booking.getRoom().getHotel().getId().equals(assignedHotelId)) {
                throw new UnauthorizedException("Bạn chỉ có quyền " + actionName + " thuộc khách sạn bạn phụ trách");
            }
            return;
        }
        throw new UnauthorizedException("Tài khoản của bạn không có quyền " + actionName);
    }

    private String extractUsername(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p) {
            return p.username();
        }
        return "System";
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal p) {
            return p.userId();
        }
        return null;
    }

    private void logAudit(Long actorId, String actorUsername, String action, String description, String entityType, Long entityId) {
        try {
            AuditLogModel log = new AuditLogModel();
            log.setActorId(actorId);
            log.setActorUsername(actorUsername);
            log.setAction(action);
            log.setActionName(action);
            log.setDescription(description);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setCreatedAt(Instant.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    @Override
    public BookingResponse mapToResponseBooking(BookingModel booking) {
        String roomNumber = "";
        String roomName = "";
        String roomType = "";
        Long roomId = null;
        Long hotelId = null;
        String hotelName = "";

        if (booking.getRoom() != null) {
            roomId = booking.getRoom().getId();
            roomName = booking.getRoom().getName();
            roomType = booking.getRoom().getType();
            if (roomName != null && roomName.matches(".*\\d+.*")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(roomName);
                if (m.find()) {
                    roomNumber = m.group();
                } else {
                    roomNumber = String.valueOf(roomId);
                }
            } else {
                roomNumber = String.valueOf(roomId);
            }

            if (booking.getRoom().getHotel() != null) {
                hotelId = booking.getRoom().getHotel().getId();
                hotelName = booking.getRoom().getHotel().getName();
            }
        }

        Long userId = null;
        String username = "";
        String customerName = "";
        String email = "";
        String phone = "";

        if (booking.getUser() != null) {
            userId = booking.getUser().getId();
            username = booking.getUser().getUsername();
            customerName = booking.getUser().getUsername();
            email = booking.getUser().getEmail();
            phone = booking.getUser().getPhone();
        }

        BigDecimal extra = booking.getExtraCharges() != null ? booking.getExtraCharges() : BigDecimal.ZERO;
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal total = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal remaining = booking.getRemainingAmount() != null
                ? booking.getRemainingAmount()
                : total.add(extra).subtract(paid);

        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(roomId)
                .roomNumber(roomNumber)
                .roomName(roomName)
                .roomType(roomType)
                .hotelId(hotelId)
                .hotelName(hotelName)
                .userId(userId)
                .username(username)
                .customerName(customerName)
                .email(email)
                .phone(phone)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .guestCount(1)
                .totalPrice(total)
                .paidAmount(paid)
                .remainingAmount(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO)
                .extraCharges(extra)
                .status(booking.getStatus())
                .stayStatus(booking.getStayStatus() != null ? booking.getStayStatus() : StayStatus.NOT_CHECKED_IN)
                .paymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus() : PaymentStatus.UNPAID)
                .paymentDeadline(booking.getPaymentDeadline())
                .actualCheckInTime(booking.getActualCheckInTime())
                .actualCheckOutTime(booking.getActualCheckOutTime())
                .checkedInBy(booking.getCheckedInBy())
                .checkedOutBy(booking.getCheckedOutBy())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
