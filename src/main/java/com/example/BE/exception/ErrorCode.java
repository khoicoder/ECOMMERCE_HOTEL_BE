package com.example.BE.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@NoArgsConstructor
public enum ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Bạn chưa đăng nhập hoặc phiên đã hết hạn"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền truy cập chức năng này"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Dữ liệu không hợp lệ"),
    VALIDATE_REQUEST(HttpStatus.BAD_REQUEST,"BAD_REQUEST","Dữ liệu gửi không hợp lệ"),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "Dữ liệu đã tồn tại"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy dữ liệu"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Lỗi hệ thống");
    private  HttpStatus status;
    private String code;
    private String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }


}
