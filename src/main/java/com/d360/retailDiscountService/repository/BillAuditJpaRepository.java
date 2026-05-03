package com.d360.retailDiscountService.repository;

import com.d360.retailDiscountService.model.entity.BillAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillAuditJpaRepository extends JpaRepository<BillAuditEntity, Long> {
}