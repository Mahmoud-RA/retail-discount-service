package com.d360.retailDiscountService.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private int errorCode;
    private String message;
    private String path;
    private List<String> details;
    private String traceId;
}