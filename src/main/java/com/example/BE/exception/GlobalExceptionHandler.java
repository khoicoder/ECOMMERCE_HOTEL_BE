package com.example.BE.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystemException(org.springframework.transaction.TransactionSystemException ex, HttpServletRequest request) {
        log.error("RANSACTION_ERROR path={} reason={}", request.getRequestURI(), ex.getMessage(), ex);
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_REQUEST.getCode(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);

    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error("DATA_INTEGRITY_ERROR path={} reason={}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_REQUEST.getCode(),
                ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : "Data integrity violation",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }


    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseAppException(
            BaseAppException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "ACCESS_DENIED path={} ip={} reason={}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage()
        );

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.FORBIDDEN.getCode(),
                ErrorCode.FORBIDDEN.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(err);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.error(
                "RUNTIME_ERROR path={} ip={} reason={}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex
        );

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_REQUEST.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(err);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("INTERNAL_ERROR", ex);

        ApiErrorResponse err = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err);
    }
}

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiErrorResponse> handleGeneralException(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        log.error(
//                "INTERNAL_ERROR path={} ip={} reason={}",
//                request.getRequestURI(),
//                request.getRemoteAddr(),
//                ex.getMessage(),
//                ex
//        );
//
//        ApiErrorResponse err = new ApiErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                ErrorCode.INTERNAL_ERROR.getCode(),
//                ErrorCode.INTERNAL_ERROR.getMessage(),
//                request.getRequestURI(),
//                LocalDateTime.now()
//        );
//
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(err);
//    }
//}