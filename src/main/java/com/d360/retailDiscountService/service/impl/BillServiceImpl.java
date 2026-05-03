package com.d360.retailDiscountService.service.impl;

import com.d360.retailDiscountService.config.properties.DiscountProperties;
import com.d360.retailDiscountService.exception.BusinessException;
import com.d360.retailDiscountService.exception.enums.ErrorCode;
import com.d360.retailDiscountService.model.document.BillDocument;
import com.d360.retailDiscountService.model.document.ItemDocument;
import com.d360.retailDiscountService.model.document.UserDocument;
import com.d360.retailDiscountService.model.document.snapshot.BillCalculationSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.BillItemSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.UserSnapshot;
import com.d360.retailDiscountService.model.dto.bills.requests.BillCalculationRequest;
import com.d360.retailDiscountService.model.dto.bills.requests.BillItemRequest;
import com.d360.retailDiscountService.model.dto.bills.response.BillCalculationResponse;
import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import com.d360.retailDiscountService.model.enums.items.ItemCategoryEnum;
import com.d360.retailDiscountService.repository.BillRepository;
import com.d360.retailDiscountService.repository.ItemRepository;
import com.d360.retailDiscountService.repository.UserRepository;
import com.d360.retailDiscountService.service.BillAuditService;
import com.d360.retailDiscountService.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillAuditService billAuditService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BillRepository billRepository;
    private final DiscountProperties discountProperties;
    @Override
    public BillCalculationResponse calculateBill(BillCalculationRequest request) {
        log.info("Fetching DB to get User Info with User ID: {}", request.getUser().getUserId());
        UserDocument user = userRepository.findByUserId(request.getUser().getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        log.info("User Info fetched successfully: {}", user);

        log.info("Fetching DB to get Items with Items: {}", request.getItems());
        List<ItemDocument> items = itemRepository.findByItemIdIn(request.getItems().stream()
                .map(BillItemRequest::getItemId).distinct().toList());
        log.info("Items fetched successfully: {}", items);

        List<BillItemSnapshot> billItems = BillItemSnapshot.toSnapshot(items, request.getItems());
        BigDecimal billAmount = billItems.stream().map(BillItemSnapshot::getTotalItemPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean loyalCustomer = user.getCustomerSince() != null && user.getCustomerSince().plusYears(2).isBefore(LocalDate.now());
        record DiscountDecision(DiscountTypeEnum type, BigDecimal rate){};
        DiscountDecision discount = switch (user.getUserType()) {
            case EMPLOYEE -> new DiscountDecision(DiscountTypeEnum.EMPLOYEE, discountProperties.getEmployeeRate());
            case AFFILIATE -> new DiscountDecision(DiscountTypeEnum.AFFILIATE, discountProperties.getAffiliateRate());
            case CUSTOMER -> loyalCustomer ? new DiscountDecision(DiscountTypeEnum.CUSTOMER_OVER_TWO_YEARS, discountProperties.getLoyaltyRate()) :
                    new DiscountDecision(DiscountTypeEnum.NONE, BigDecimal.ZERO);
        };
        log.info("Applying Discount Decision: {}", discount);
        BigDecimal discountAmount = billItems.stream()
                .filter(item -> item.getCategory() != ItemCategoryEnum.GROCERY)
                .map(BillItemSnapshot::getTotalItemPrice).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(discount.rate())
                .add(billAmount.divideToIntegralValue(discountProperties.getFixedDiscountStepAmount()).multiply(discountProperties.getFixedDiscountValue()));
        BigDecimal netPayableAmount = billAmount.subtract(discountAmount);
        BillDocument savedBill = billRepository.save(BillDocument.builder()
                                .user(UserSnapshot.builder()
                                        .userId(user.getUserId())
                                        .userType(user.getUserType())
                                        .customerSince(user.getCustomerSince())
                                        .build())
                                .items(billItems)
                                .calculation(BillCalculationSnapshot.builder()
                                        .billAmount(billAmount)
                                        .appliedDiscountType(discount.type())
                                        .discountAmount(discountAmount)
                                        .netPayableAmount(netPayableAmount)
                                        .build())
                                .createdAt(LocalDateTime.now())
                                .build());
        billAuditService.audit(savedBill);
        return BillCalculationResponse.builder()
                .billId(savedBill.getId())
                .billAmount(billAmount)
                .appliedDiscountType(discount.type())
                .discountAmount(discountAmount)
                .netPayableAmount(netPayableAmount)
                .build();
    }

    @Override
    public BillCalculationResponse getBillById(String billId) {
        log.info("Fetching DB to get Bill with Bill ID: {}", billId);
        BillDocument bill = billRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILL_NOT_FOUND));
        log.info("Bill fetched successfully: {}", bill);
        return BillCalculationResponse.builder()
                .billId(bill.getId())
                .billAmount(bill.getCalculation().getBillAmount())
                .appliedDiscountType(bill.getCalculation().getAppliedDiscountType())
                .discountAmount(bill.getCalculation().getDiscountAmount())
                .netPayableAmount(bill.getCalculation().getNetPayableAmount())
                .build();
    }

    @Override
    public Page<BillCalculationResponse> getBillsByUserId(Long userId, int page, int size) {
        log.info("Fetching DB to check User existence with User ID: {}", userId);
        userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        log.info("Fetching DB to get Bills with User ID: {} and page: {}, size: {}", userId, page, size);
        return billRepository.findByUserUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(bill -> BillCalculationResponse.builder()
                .billId(bill.getId())
                .billAmount(bill.getCalculation().getBillAmount())
                .appliedDiscountType(bill.getCalculation().getAppliedDiscountType())
                .discountAmount(bill.getCalculation().getDiscountAmount())
                .netPayableAmount(bill.getCalculation().getNetPayableAmount())
                .build());
    }
}
