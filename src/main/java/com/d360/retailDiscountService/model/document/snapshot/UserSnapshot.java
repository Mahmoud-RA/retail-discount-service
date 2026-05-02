package com.d360.retailDiscountService.model.document.snapshot;

import com.d360.retailDiscountService.model.enums.users.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    private Long userId;
    private UserTypeEnum userType;
    private LocalDate customerSince;
}
