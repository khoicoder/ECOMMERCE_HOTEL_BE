package com.example.BE.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiErrorResponse {
    private int status;
    private String errorCode;

    private String message;
    private String path;
    private LocalDateTime timestamp;


}
