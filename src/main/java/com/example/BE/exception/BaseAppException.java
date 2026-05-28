package com.example.BE.exception;

public class BaseAppException extends RuntimeException {
    private final ErrorCode errorCode;
    public BaseAppException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }
    public BaseAppException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
