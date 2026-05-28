package com.example.BE.exception;

public class ValidateException extends BaseAppException {
    public ValidateException() {
        super(ErrorCode.VALIDATE_REQUEST);
    }
    public ValidateException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
