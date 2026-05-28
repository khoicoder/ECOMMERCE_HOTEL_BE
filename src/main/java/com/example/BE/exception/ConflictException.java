package com.example.BE.exception;

public class ConflictException extends BaseAppException {
    public ConflictException() {
        super(ErrorCode.CONFLICT);
    }
    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);

    }
}
