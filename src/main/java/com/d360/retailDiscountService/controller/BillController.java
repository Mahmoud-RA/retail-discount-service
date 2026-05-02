package com.d360.retailDiscountService.controller;

import com.d360.retailDiscountService.model.dto.bills.requests.BillCalculationRequest;
import com.d360.retailDiscountService.model.dto.bills.response.BillCalculationResponse;
import com.d360.retailDiscountService.service.BillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BillController {

    private final BillService billService;


    @PostMapping(value = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BillCalculationResponse> calculateBill(@Valid @RequestBody BillCalculationRequest billCalculationReq) {
        log.info("Received bill calculation request: {}", billCalculationReq);
        BillCalculationResponse response = billService.calculateBill(billCalculationReq);
        log.info("Bill calculation response: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<BillCalculationResponse> getBillById(@PathVariable @NotBlank(message = "Bill id is required") String billId) {
        log.info("Received get bill request, billId={}", billId);
        BillCalculationResponse billCalculationResponse = billService.getBillById(billId);
        log.info("Bill calculation response: {}", billCalculationResponse);
        return ResponseEntity.ok(billCalculationResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BillCalculationResponse>> getBillsByUserId(@PathVariable @NotNull(message = "User id is required") Long userId,
                                                                          @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater") int page,
                                                                          @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1")
                                                                          @Max(value = 100, message = "Size must not exceed 100") int size) {
        log.info("Received get bills by user request, userId={}", userId);
        return ResponseEntity.ok(billService.getBillsByUserId(userId, page, size));
    }
}
