package com.d360.retailDiscountService.service;

import com.d360.retailDiscountService.model.document.BillDocument;

public interface BillAuditService {

    void audit(BillDocument billDocument);
}