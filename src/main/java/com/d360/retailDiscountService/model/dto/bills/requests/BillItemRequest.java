package com.d360.retailDiscountService.model.dto.bills.requests;

import com.d360.retailDiscountService.model.enums.items.ItemCategoryEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemRequest {

    @NotNull(message = "Item id is required")
    private Long itemId;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}