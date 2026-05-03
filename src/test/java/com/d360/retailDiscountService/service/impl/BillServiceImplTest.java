package com.d360.retailDiscountService.service.impl;

import com.d360.retailDiscountService.config.properties.DiscountProperties;
import com.d360.retailDiscountService.exception.BusinessException;
import com.d360.retailDiscountService.exception.enums.ErrorCode;
import com.d360.retailDiscountService.model.document.BillDocument;
import com.d360.retailDiscountService.model.document.ItemDocument;
import com.d360.retailDiscountService.model.document.UserDocument;
import com.d360.retailDiscountService.model.document.snapshot.BillCalculationSnapshot;
import com.d360.retailDiscountService.model.document.snapshot.UserSnapshot;
import com.d360.retailDiscountService.model.dto.bills.requests.BillCalculationRequest;
import com.d360.retailDiscountService.model.dto.bills.requests.BillItemRequest;
import com.d360.retailDiscountService.model.dto.bills.requests.users.UserRequest;
import com.d360.retailDiscountService.model.dto.bills.response.BillCalculationResponse;
import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import com.d360.retailDiscountService.model.enums.items.ItemCategoryEnum;
import com.d360.retailDiscountService.model.enums.users.UserTypeEnum;
import com.d360.retailDiscountService.repository.BillRepository;
import com.d360.retailDiscountService.repository.ItemRepository;
import com.d360.retailDiscountService.repository.UserRepository;
import com.d360.retailDiscountService.service.BillAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillAuditService billAuditService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private DiscountProperties discountProperties;

    @InjectMocks
    private BillServiceImpl billService;

    @Test
    void calculateBill_employeeWithGroceryAndNonGrocery_shouldApplyEmployeeAndFixedDiscount() {
        mockEmployeeDiscount();

        BillCalculationRequest request = request(1001L,
                itemRequest(2001L, 2),
                itemRequest(2002L, 1)
        );

        when(userRepository.findByUserId(1001L))
                .thenReturn(Optional.of(user(1001L, UserTypeEnum.EMPLOYEE, LocalDate.of(2020, 1, 15))));

        when(itemRepository.findByItemIdIn(List.of(2001L, 2002L)))
                .thenReturn(List.of(
                        item(2001L, "Rice", ItemCategoryEnum.GROCERY, "50.00"),
                        item(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")
                ));

        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-1");
            return bill;
        });

        BillCalculationResponse response = billService.calculateBill(request);

        assertThat(response.getBillId()).isEqualTo("bill-1");
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.EMPLOYEE);
        assertThat(response.getBillAmount()).isEqualByComparingTo("400.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("110.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("290.00");

        verify(userRepository).findByUserId(1001L);
        verify(itemRepository).findByItemIdIn(List.of(2001L, 2002L));
        verify(billRepository).save(any(BillDocument.class));
        verify(billAuditService).audit(any(BillDocument.class));
    }

    @Test
    void calculateBill_affiliate_shouldApplyAffiliateAndFixedDiscount() {
        mockAffiliateDiscount();
        BillCalculationRequest request = request(1002L, itemRequest(2002L, 1));
        when(userRepository.findByUserId(1002L)).thenReturn(Optional.of(user(1002L, UserTypeEnum.AFFILIATE, LocalDate.of(2022, 3, 10))));
        when(itemRepository.findByItemIdIn(List.of(2002L))).thenReturn(List.of(item(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")));
        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-2");
            return bill;
        });
        BillCalculationResponse response = billService.calculateBill(request);
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.AFFILIATE);
        assertThat(response.getBillAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("45.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("255.00");
        verify(billAuditService).audit(any(BillDocument.class));
    }

    @Test
    void calculateBill_loyalCustomer_shouldApplyLoyaltyAndFixedDiscount() {
        mockLoyaltyDiscount();
        BillCalculationRequest request = request(1003L, itemRequest(2002L, 1));
        when(userRepository.findByUserId(1003L)).thenReturn(Optional.of(user(1003L, UserTypeEnum.CUSTOMER, LocalDate.now().minusYears(3))));
        when(itemRepository.findByItemIdIn(List.of(2002L))).thenReturn(List.of(item(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")));
        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-3");
            return bill;
        });
        BillCalculationResponse response = billService.calculateBill(request);
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.CUSTOMER_OVER_TWO_YEARS);
        assertThat(response.getBillAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("270.00");
        verify(billAuditService).audit(any(BillDocument.class));
    }

    @Test
    void calculateBill_newCustomer_shouldApplyOnlyFixedDiscount() {
        mockFixedDiscount();
        BillCalculationRequest request = request(1004L, itemRequest(2002L, 1));
        when(userRepository.findByUserId(1004L)).thenReturn(Optional.of(user(1004L, UserTypeEnum.CUSTOMER, LocalDate.now().minusMonths(6))));
        when(itemRepository.findByItemIdIn(List.of(2002L))).thenReturn(List.of(item(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")));
        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-4");
            return bill;
        });
        BillCalculationResponse response = billService.calculateBill(request);
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.NONE);
        assertThat(response.getBillAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("15.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("285.00");
        verify(billAuditService).audit(any(BillDocument.class));
    }

    @Test
    void calculateBill_employeeWithOnlyGroceries_shouldExcludeGroceriesFromPercentageDiscount() {
        mockEmployeeDiscount();
        BillCalculationRequest request = request(1001L, itemRequest(2001L, 2));
        when(userRepository.findByUserId(1001L)).thenReturn(Optional.of(user(1001L, UserTypeEnum.EMPLOYEE, LocalDate.of(2020, 1, 15))));
        when(itemRepository.findByItemIdIn(List.of(2001L))).thenReturn(List.of(item(2001L, "Rice", ItemCategoryEnum.GROCERY, "50.00")));
        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-5");
            return bill;
        });
        BillCalculationResponse response = billService.calculateBill(request);
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.EMPLOYEE);
        assertThat(response.getBillAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("5.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("95.00");
        verify(billAuditService).audit(any(BillDocument.class));
    }

    @Test
    void calculateBill_userNotFound_shouldThrowBusinessException() {
        BillCalculationRequest request = request(9999L,
                itemRequest(2001L, 1)
        );

        when(userRepository.findByUserId(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.calculateBill(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verifyNoInteractions(itemRepository, billRepository);
        verify(billAuditService, never()).audit(any());
    }

    @Test
    void calculateBill_itemNotFound_shouldThrowBusinessException() {
        BillCalculationRequest request = request(1001L,
                itemRequest(9999L, 1)
        );

        when(userRepository.findByUserId(1001L))
                .thenReturn(Optional.of(user(1001L, UserTypeEnum.EMPLOYEE, LocalDate.of(2020, 1, 15))));

        when(itemRepository.findByItemIdIn(List.of(9999L))).thenReturn(List.of());

        assertThatThrownBy(() -> billService.calculateBill(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ITEM_NOT_FOUND);
        verify(billAuditService, never()).audit(any());
        verify(billRepository, never()).save(any());
    }

    @Test
    void getBillById_billExists_shouldReturnBill() {
        BillDocument bill = bill("bill-100", 1001L);

        when(billRepository.findById("bill-100")).thenReturn(Optional.of(bill));

        BillCalculationResponse response = billService.getBillById("bill-100");

        assertThat(response.getBillId()).isEqualTo("bill-100");
        assertThat(response.getAppliedDiscountType()).isEqualTo(DiscountTypeEnum.EMPLOYEE);
        assertThat(response.getBillAmount()).isEqualByComparingTo("400.00");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("110.00");
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo("290.00");
    }

    @Test
    void getBillById_billMissing_shouldThrowBusinessException() {
        when(billRepository.findById("missing-bill")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.getBillById("missing-bill"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BILL_NOT_FOUND);
    }

    @Test
    void getBillsByUserId_userExists_shouldReturnPagedBills() {
        when(userRepository.findByUserId(1001L))
                .thenReturn(Optional.of(user(1001L, UserTypeEnum.EMPLOYEE, LocalDate.of(2020, 1, 15))));

        Page<BillDocument> page = new PageImpl<>(
                List.of(bill("bill-200", 1001L)),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );

        when(billRepository.findByUserUserId(eq(1001L), any(Pageable.class)))
                .thenReturn(page);

        Page<BillCalculationResponse> response = billService.getBillsByUserId(1001L, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getBillId()).isEqualTo("bill-200");
        assertThat(response.getContent().get(0).getNetPayableAmount()).isEqualByComparingTo("290.00");
    }

    @Test
    void getBillsByUserId_userMissing_shouldThrowBusinessException() {
        when(userRepository.findByUserId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.getBillsByUserId(404L, 0, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(billRepository, never()).findByUserUserId(anyLong(), any(Pageable.class));
    }

    @ParameterizedTest
    @MethodSource("billCalculationCases")
    void calculateBill_shouldCalculateCorrectly(String testName, UserTypeEnum userType, LocalDate customerSince, List<BillItemRequest> requestItems,
            List<ItemDocument> dbItems, DiscountTypeEnum expectedDiscountType, String expectedBillAmount, String expectedDiscountAmount,
            String expectedNetPayableAmount) {
        mockDiscountPropertiesFor(userType, customerSince);
        BillCalculationRequest request = BillCalculationRequest.builder()
                .user(UserRequest.builder().userId(1001L).build())
                .items(requestItems)
                .build();
        when(userRepository.findByUserId(1001L))
                .thenReturn(Optional.of(user(1001L, userType, customerSince)));
        when(itemRepository.findByItemIdIn(
                requestItems.stream().map(BillItemRequest::getItemId).distinct().toList()
        )).thenReturn(dbItems);
        when(billRepository.save(any(BillDocument.class))).thenAnswer(invocation -> {
            BillDocument bill = invocation.getArgument(0);
            bill.setId("bill-test");
            return bill;
        });
        BillCalculationResponse response = billService.calculateBill(request);
        assertThat(response.getAppliedDiscountType()).as(testName).isEqualTo(expectedDiscountType);
        assertThat(response.getBillAmount()).isEqualByComparingTo(expectedBillAmount);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(expectedDiscountAmount);
        assertThat(response.getNetPayableAmount()).isEqualByComparingTo(expectedNetPayableAmount);
    }

    private void mockDiscountPropertiesFor(UserTypeEnum userType, LocalDate customerSince) {
        switch (userType) {
            case EMPLOYEE -> when(discountProperties.getEmployeeRate()).thenReturn(new BigDecimal("0.30"));
            case AFFILIATE -> when(discountProperties.getAffiliateRate()).thenReturn(new BigDecimal("0.10"));
            case CUSTOMER -> {
                boolean loyalCustomer = customerSince != null && customerSince.plusYears(2).isBefore(LocalDate.now());
                if (loyalCustomer) when(discountProperties.getLoyaltyRate()).thenReturn(new BigDecimal("0.05"));
            }
        }
        when(discountProperties.getFixedDiscountStepAmount()).thenReturn(new BigDecimal("100"));
        when(discountProperties.getFixedDiscountValue()).thenReturn(new BigDecimal("5"));
    }

    private static Stream<Arguments> billCalculationCases() {
        return Stream.of(Arguments.of("Employee: grocery + non-grocery", UserTypeEnum.EMPLOYEE, LocalDate.of(2020, 1, 15),
                        List.of(itemRequestStatic(2001L, 2), itemRequestStatic(2002L, 1)),
                        List.of(itemStatic(2001L, "Rice", ItemCategoryEnum.GROCERY, "50.00"),
                                itemStatic(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")), DiscountTypeEnum.EMPLOYEE,
                        "400.00", "110.00", "290.00"),
                Arguments.of("Affiliate: non-grocery only", UserTypeEnum.AFFILIATE, LocalDate.of(2022, 3, 10),
                        List.of(itemRequestStatic(2002L, 1)), List.of(itemStatic(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")),
                        DiscountTypeEnum.AFFILIATE, "300.00", "45.00", "255.00"),
                Arguments.of("Loyal customer", UserTypeEnum.CUSTOMER, LocalDate.now().minusYears(3),
                        List.of(itemRequestStatic(2002L, 1)), List.of(itemStatic(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")),
                        DiscountTypeEnum.CUSTOMER_OVER_TWO_YEARS, "300.00", "30.00", "270.00"),
                Arguments.of("New customer", UserTypeEnum.CUSTOMER, LocalDate.now().minusMonths(6),
                        List.of(itemRequestStatic(2002L, 1)), List.of(itemStatic(2002L, "Headphones", ItemCategoryEnum.OTHER, "300.00")),
                        DiscountTypeEnum.NONE, "300.00", "15.00", "285.00"));
    }

    private static BillItemRequest itemRequestStatic(Long itemId, Integer quantity) {
        return BillItemRequest.builder()
                .itemId(itemId)
                .quantity(quantity)
                .build();
    }

    private static ItemDocument itemStatic(Long itemId, String name, ItemCategoryEnum category, String unitPrice) {
        return ItemDocument.builder()
                .id("mongo-item-" + itemId)
                .itemId(itemId)
                .name(name)
                .category(category)
                .unitPrice(new BigDecimal(unitPrice))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private BillCalculationRequest request(Long userId, BillItemRequest... items) {
        return BillCalculationRequest.builder()
                .user(UserRequest.builder()
                        .userId(userId)
                        .build())
                .items(List.of(items))
                .build();
    }

    private BillItemRequest itemRequest(Long itemId, Integer quantity) {
        return BillItemRequest.builder()
                .itemId(itemId)
                .quantity(quantity)
                .build();
    }

    private UserDocument user(Long userId, UserTypeEnum userType, LocalDate customerSince) {
        return UserDocument.builder()
                .id("mongo-user-" + userId)
                .userId(userId)
                .userType(userType)
                .customerSince(customerSince)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ItemDocument item(Long itemId, String name, ItemCategoryEnum category, String unitPrice) {
        return ItemDocument.builder()
                .id("mongo-item-" + itemId)
                .itemId(itemId)
                .name(name)
                .category(category)
                .unitPrice(new BigDecimal(unitPrice))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private BillDocument bill(String id, Long userId) {
        return BillDocument.builder()
                .id(id)
                .user(UserSnapshot.builder()
                        .userId(userId)
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
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void mockEmployeeDiscount() {
        when(discountProperties.getEmployeeRate()).thenReturn(new BigDecimal("0.30"));
        mockFixedDiscount();
    }

    private void mockAffiliateDiscount() {
        when(discountProperties.getAffiliateRate()).thenReturn(new BigDecimal("0.10"));
        mockFixedDiscount();
    }

    private void mockLoyaltyDiscount() {
        when(discountProperties.getLoyaltyRate()).thenReturn(new BigDecimal("0.05"));
        mockFixedDiscount();
    }

    private void mockFixedDiscount() {
        when(discountProperties.getFixedDiscountStepAmount()).thenReturn(new BigDecimal("100"));
        when(discountProperties.getFixedDiscountValue()).thenReturn(new BigDecimal("5"));
    }
}