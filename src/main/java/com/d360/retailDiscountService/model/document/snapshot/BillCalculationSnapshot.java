package com.d360.retailDiscountService.model.document.snapshot;

import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCalculationSnapshot {

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal billAmount;

    private DiscountTypeEnum appliedDiscountType;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal discountAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal netPayableAmount;
}