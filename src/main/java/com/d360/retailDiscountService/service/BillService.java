package com.d360.retailDiscountService.service;

import com.d360.retailDiscountService.model.dto.bills.requests.BillCalculationRequest;
import com.d360.retailDiscountService.model.dto.bills.response.BillCalculationResponse;
import org.springframework.data.domain.Page;

public interface BillService {

    BillCalculationResponse calculateBill(BillCalculationRequest billCalculationReq);

    BillCalculationResponse getBillById(String billId);

    Page<BillCalculationResponse> getBillsByUserId(Long userId, int page, int size);
}