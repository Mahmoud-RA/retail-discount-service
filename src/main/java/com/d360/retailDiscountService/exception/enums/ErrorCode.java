package com.d360.retailDiscountService.exception.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "User not found"),
    ITEM_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Item not found"),
    BILL_NOT_FOUND(1003, HttpStatus.NOT_FOUND, "Bill not found"),

    VALIDATION_ERROR(2001, HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_REQUEST(2002, HttpStatus.BAD_REQUEST, "Invalid request"),

    UNAUTHORIZED(3001, HttpStatus.UNAUTHORIZED, "Invalid client id or secret"),

    INTERNAL_ERROR(9001, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}