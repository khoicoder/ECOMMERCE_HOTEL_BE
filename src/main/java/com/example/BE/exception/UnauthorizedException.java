package com.example.BE.exception;

public class UnauthorizedException extends BaseAppException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode.UNAUTHORIZED);
    }
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
