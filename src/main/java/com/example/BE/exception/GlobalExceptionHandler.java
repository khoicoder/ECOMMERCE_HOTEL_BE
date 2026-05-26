package com.example.BE.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import javax.naming.ConfigurationException;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<?> handleUnauthorized(HttpClientErrorException.Unauthorized exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()

        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);

    }
    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<?> handleConfigurationException(ConfigurationException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return  ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST).body(err);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidateException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = "Dữ liệu không hợp lệ";
                if(!ex.getBindingResult().getFieldErrors().isEmpty()) {
                        message = ex.getBindingResult()
                            .getFieldErrors()
                            .get(0)
                            .getDefaultMessage();
                }


        ApiErrorResponse err = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(err);

    }
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
       log.warn("ACCESS_DENIED path={} ip={} reason={}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage()
        );

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Bạn không có quyền truy cập chức năng này...",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return  ResponseEntity
                .status(HttpStatus.FORBIDDEN).body(err);

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "lỗi hệ thống..."+ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return  ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }




}
