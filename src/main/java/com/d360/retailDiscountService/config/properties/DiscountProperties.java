package com.d360.retailDiscountService.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "app.discount")
public class DiscountProperties {

    private BigDecimal employeeRate;

    private BigDecimal affiliateRate;

    private BigDecimal loyaltyRate;

    private BigDecimal fixedDiscountStepAmount;

    private BigDecimal fixedDiscountValue;
}