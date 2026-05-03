package com.d360.retailDiscountService.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bill_audit")
public class BillAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_id", nullable = false, length = 100)
    private String billId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "applied_discount_type", nullable = false, length = 50)
    private String appliedDiscountType;

    @Column(name = "bill_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "net_payable_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPayableAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}