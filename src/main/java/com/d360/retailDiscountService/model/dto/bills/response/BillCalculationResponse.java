package com.d360.retailDiscountService.model.dto.bills.response;

import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCalculationResponse {

    private String billId;

    private DiscountTypeEnum appliedDiscountType;

    private BigDecimal billAmount;

    private BigDecimal discountAmount;

    private BigDecimal netPayableAmount;
}
