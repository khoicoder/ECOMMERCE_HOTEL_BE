package com.example.BE.exception;

public class ValidateException extends BaseAppException {
    public ValidateException() {
        super(ErrorCode.VALIDATE_REQUEST);
    }
    public ValidateException(String detailMessage) {
        super(ErrorCode.VALIDATE_REQUEST, detailMessage);
    }
}
