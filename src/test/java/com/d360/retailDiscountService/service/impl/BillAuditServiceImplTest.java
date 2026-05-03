package com.d360.retailDiscountService.service.impl;

import com.d360.retailDiscountService.model.document.BillDocument;
import com.d360.retailDiscountService.model.document.snapshot.BillCalculationSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.UserSnapshot;
import com.d360.retailDiscountService.model.entity.BillAuditEntity;
import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import com.d360.retailDiscountService.model.enums.users.UserTypeEnum;
import com.d360.retailDiscountService.repository.BillAuditJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillAuditServiceImplTest {

    @Mock
    private BillAuditJpaRepository billAuditJpaRepository;

    @InjectMocks
    private BillAuditServiceImpl billAuditService;

    @Test
    void audit_shouldSaveBillAuditEntity() {
        LocalDateTime createdAt = LocalDateTime.now();

        BillDocument billDocument = BillDocument.builder()
                .id("bill-100")
                .user(UserSnapshot.builder()
                        .userId(1001L)
                        .userType(UserTypeEnum.EMPLOYEE)
                        .customerSince(LocalDate.of(2020, 1, 15))
                        .build())
                .items(List.of())
                .calculation(BillCalculationSnapshot.builder()
                        .billAmount(new BigDecimal("400.00"))
                        .appliedDiscountType(DiscountTypeEnum.EMPLOYEE)
                        .discountAmount(new BigDecimal("110.00"))
                        .netPayableAmount(new BigDecimal("290.00"))
                        .build())
                .createdAt(createdAt)
                .build();

        billAuditService.audit(billDocument);

        ArgumentCaptor<BillAuditEntity> captor = ArgumentCaptor.forClass(BillAuditEntity.class);
        verify(billAuditJpaRepository).save(captor.capture());

        BillAuditEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getBillId()).isEqualTo("bill-100");
        assertThat(savedEntity.getUserId()).isEqualTo(1001L);
        assertThat(savedEntity.getAppliedDiscountType()).isEqualTo("EMPLOYEE");
        assertThat(savedEntity.getBillAmount()).isEqualByComparingTo("400.00");
        assertThat(savedEntity.getDiscountAmount()).isEqualByComparingTo("110.00");
        assertThat(savedEntity.getNetPayableAmount()).isEqualByComparingTo("290.00");
        assertThat(savedEntity.getCreatedAt()).isEqualTo(createdAt);
    }
}