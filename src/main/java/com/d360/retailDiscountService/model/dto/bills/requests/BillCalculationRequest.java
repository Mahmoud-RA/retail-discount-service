package com.d360.retailDiscountService.model.dto.bills.requests;

import com.d360.retailDiscountService.model.dto.bills.requests.users.UserRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCalculationRequest {

    @NotNull(message = "User is required")
    @Valid
    private UserRequest user;

    @NotEmpty(message = "Bill items are required")
    private List<@Valid BillItemRequest> items;

}