package com.d360.retailDiscountService.config.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class SecurityConstants {
    public static final String HEALTH_CHECK_PATH = "/actuator/health";
    public static final String BILLS_API_PATH = "/bills/**";
    public static final String ERROR_PATH = "/error";
    public static final String HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HEADER_CLIENT_SECRET = "X-Client-Secret";
    public static final String ROLE_ADMIN = "ADMIN";
}