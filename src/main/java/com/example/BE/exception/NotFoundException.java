package com.example.BE.exception;

public class NotFoundException extends BaseAppException {
    public NotFoundException(){
        super(ErrorCode.NOT_FOUND);
    }
    public NotFoundException( String detailMessage){
        super(ErrorCode.NOT_FOUND, detailMessage);
    }
}
