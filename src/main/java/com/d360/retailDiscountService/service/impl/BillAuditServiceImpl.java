package com.d360.retailDiscountService.service.impl;

import com.d360.retailDiscountService.model.document.BillDocument;
import com.d360.retailDiscountService.model.entity.BillAuditEntity;
import com.d360.retailDiscountService.repository.BillAuditJpaRepository;
import com.d360.retailDiscountService.service.BillAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillAuditServiceImpl implements BillAuditService {

    private final BillAuditJpaRepository billAuditJpaRepository;

    @Override
    public void audit(BillDocument billDocument) {
        billAuditJpaRepository.save(BillAuditEntity.builder()
                .billId(billDocument.getId())
                .userId(billDocument.getUser().getUserId())
                .appliedDiscountType(billDocument.getCalculation().getAppliedDiscountType().name())
                .billAmount(billDocument.getCalculation().getBillAmount())
                .discountAmount(billDocument.getCalculation().getDiscountAmount())
                .netPayableAmount(billDocument.getCalculation().getNetPayableAmount())
                .createdAt(billDocument.getCreatedAt())
                .build());
    }
}