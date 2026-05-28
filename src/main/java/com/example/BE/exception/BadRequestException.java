package com.example.BE.exception;

public class BadRequestException extends BaseAppException {
    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

}
