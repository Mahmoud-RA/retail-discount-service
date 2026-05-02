package com.d360.retailDiscountService.controller;

import com.d360.retailDiscountService.config.security.SecurityConfig;
import com.d360.retailDiscountService.exception.BusinessException;
import com.d360.retailDiscountService.exception.GlobalExceptionHandler;
import com.d360.retailDiscountService.exception.enums.ErrorCode;
import com.d360.retailDiscountService.model.dto.bills.requests.BillCalculationRequest;
import com.d360.retailDiscountService.model.dto.bills.requests.BillItemRequest;
import com.d360.retailDiscountService.model.dto.bills.requests.users.UserRequest;
import com.d360.retailDiscountService.model.dto.bills.response.BillCalculationResponse;
import com.d360.retailDiscountService.model.enums.bills.DiscountTypeEnum;
import com.d360.retailDiscountService.service.BillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.security.client-id=retail-app-v1",
        "app.security.client-secret=super-secret-key-789"
})
public class BillControllerTest {

    private static final String CLIENT_ID_HEADER = "X-Client-Id";
    private static final String CLIENT_SECRET_HEADER = "X-Client-Secret";

    private static final String VALID_CLIENT_ID = "retail-app-v1";
    private static final String VALID_CLIENT_SECRET = "super-secret-key-789";

    private static final String BILL_CALCULATE_ENDPOINT = "/bills/calculate";
    private static final String BILL_GET_BY_ID_ENDPOINT = "/bills/{billId}";
    private static final String BILL_GET_BY_USER_ID_ENDPOINT = "/bills/user/{userId}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BillService billService;

    // ======================================================================
    // Testing ALL Scenarios of Bill Calculation API
    // ======================================================================

    /*
        case 1: UNAUTHORIZED because clientId & clientSecret were not provided
     */
    @Test
    void calculateBill_withoutApiHeaders_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(post(BILL_CALCULATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCalculateRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 2: UNAUTHORIZED because invalid clientId & clientSecret were provided
     */
    @Test
    void calculateBill_provideInvalidAuthorization_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(post(BILL_CALCULATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CLIENT_ID_HEADER, "XXX3331")
                        .header(CLIENT_SECRET_HEADER, "XXX3331")
                        .content(objectMapper.writeValueAsString(validCalculateRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 3: BAD REQUEST because invalid request was provided
     */
    @Test
    void calculateBill_provideInvalidRequest_shouldReturnBadRequestAndNotCallService() throws Exception {
        mockMvc.perform(post(BILL_CALCULATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .content(objectMapper.writeValueAsString(inValidCalculateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(2001))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(billService);
    }

    /*
        case 4: 201 CREATED valid request and should pass security // SUCCESS CASE
     */
    @Test
    void calculateBill_provideValidRequest_shouldReturnSuccessAndCallService() throws Exception {
        when(billService.calculateBill(any(BillCalculationRequest.class)))
                .thenReturn(validCalculateResponse());

        mockMvc.perform(post(BILL_CALCULATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .content(objectMapper.writeValueAsString(validCalculateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.billId").value("bill-1"))
                .andExpect(jsonPath("$.appliedDiscountType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.billAmount").value(400.00))
                .andExpect(jsonPath("$.discountAmount").value(110.00))
                .andExpect(jsonPath("$.netPayableAmount").value(290.00));

        verify(billService).calculateBill(any(BillCalculationRequest.class));
    }

    /*
        case 5: NOT FOUND because service throws business exception
     */
    @Test
    void calculateBill_serviceThrowsBusinessException_shouldReturnMappedBusinessError() throws Exception {
        when(billService.calculateBill(any(BillCalculationRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post(BILL_CALCULATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .content(objectMapper.writeValueAsString(validCalculateRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(1001))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(billService).calculateBill(any(BillCalculationRequest.class));
    }

    // ======================================================================
    // Testing ALL Scenarios of Get Bill By Id API
    // ======================================================================

    /*
        case 1: UNAUTHORIZED because clientId & clientSecret were not provided
     */
    @Test
    void getBillById_withoutApiHeaders_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_ID_ENDPOINT, "bill-100"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 2: UNAUTHORIZED because invalid clientId & clientSecret were provided
     */
    @Test
    void getBillById_provideInvalidAuthorization_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_ID_ENDPOINT, "bill-100")
                        .header(CLIENT_ID_HEADER, "XXX3331")
                        .header(CLIENT_SECRET_HEADER, "XXX3331"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 3: BAD REQUEST because invalid billId was provided
     */
    @Test
    void getBillById_provideInvalidRequest_shouldReturnBadRequestAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_ID_ENDPOINT, " ")
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(2001))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(billService);
    }

    /*
        case 4: 200 OK valid request and should pass security // SUCCESS CASE
     */
    @Test
    void getBillById_provideValidRequest_shouldReturnSuccessAndCallService() throws Exception {
        when(billService.getBillById("bill-100"))
                .thenReturn(validCalculateResponse("bill-100"));

        mockMvc.perform(get(BILL_GET_BY_ID_ENDPOINT, "bill-100")
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billId").value("bill-100"))
                .andExpect(jsonPath("$.appliedDiscountType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.billAmount").value(400.00))
                .andExpect(jsonPath("$.discountAmount").value(110.00))
                .andExpect(jsonPath("$.netPayableAmount").value(290.00));

        verify(billService).getBillById("bill-100");
    }

    /*
        case 5: NOT FOUND because bill does not exist
     */
    @Test
    void getBillById_serviceThrowsBusinessException_shouldReturnMappedBusinessError() throws Exception {
        when(billService.getBillById("missing-bill"))
                .thenThrow(new BusinessException(ErrorCode.BILL_NOT_FOUND));

        mockMvc.perform(get(BILL_GET_BY_ID_ENDPOINT, "missing-bill")
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(1003))
                .andExpect(jsonPath("$.message").value("Bill not found"));

        verify(billService).getBillById("missing-bill");
    }

    // ======================================================================
    // Testing ALL Scenarios of Get Bills By User Id API
    // ======================================================================

    /*
        case 1: UNAUTHORIZED because clientId & clientSecret were not provided
     */
    @Test
    void getBillsByUserId_withoutApiHeaders_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 1001L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 2: UNAUTHORIZED because invalid clientId & clientSecret were provided
     */
    @Test
    void getBillsByUserId_provideInvalidAuthorization_shouldReturnUnauthorizedAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 1001L)
                        .header(CLIENT_ID_HEADER, "XXX3331")
                        .header(CLIENT_SECRET_HEADER, "XXX3331")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(billService);
    }

    /*
        case 3: BAD REQUEST because invalid page was provided
     */
    @Test
    void getBillsByUserId_provideInvalidPage_shouldReturnBadRequestAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 1001L)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(2001))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(billService);
    }

    /*
        case 4: BAD REQUEST because invalid size was provided
     */
    @Test
    void getBillsByUserId_provideInvalidSize_shouldReturnBadRequestAndNotCallService() throws Exception {
        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 1001L)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(2001))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(billService);
    }

    /*
        case 5: 200 OK valid request and should return paginated response // SUCCESS CASE
     */
    @Test
    void getBillsByUserId_provideValidRequest_shouldReturnPagedSuccessAndCallService() throws Exception {
        Page<BillCalculationResponse> page = new PageImpl<>(
                List.of(validCalculateResponse("bill-200")),
                PageRequest.of(0, 10),
                1
        );

        when(billService.getBillsByUserId(1001L, 0, 10))
                .thenReturn(page);

        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 1001L)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].billId").value("bill-200"))
                .andExpect(jsonPath("$.content[0].appliedDiscountType").value("EMPLOYEE"))
                .andExpect(jsonPath("$.content[0].billAmount").value(400.00))
                .andExpect(jsonPath("$.content[0].discountAmount").value(110.00))
                .andExpect(jsonPath("$.content[0].netPayableAmount").value(290.00))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(billService).getBillsByUserId(1001L, 0, 10);
    }

    /*
        case 6: NOT FOUND because user does not exist
     */
    @Test
    void getBillsByUserId_serviceThrowsBusinessException_shouldReturnMappedBusinessError() throws Exception {
        when(billService.getBillsByUserId(9999L, 0, 10))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get(BILL_GET_BY_USER_ID_ENDPOINT, 9999L)
                        .header(CLIENT_ID_HEADER, VALID_CLIENT_ID)
                        .header(CLIENT_SECRET_HEADER, VALID_CLIENT_SECRET)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(1001))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(billService).getBillsByUserId(9999L, 0, 10);
    }

    private BillCalculationRequest validCalculateRequest() {
        return BillCalculationRequest.builder()
                .user(UserRequest.builder()
                        .userId(1001L)
                        .build())
                .items(List.of(
                        BillItemRequest.builder()
                                .itemId(2001L)
                                .quantity(2)
                                .build()
                ))
                .build();
    }

    private BillCalculationRequest inValidCalculateRequest() {
        return BillCalculationRequest.builder()
                .user(UserRequest.builder()
                        .build())
                .items(List.of(
                        BillItemRequest.builder()
                                .itemId(2001L)
                                .quantity(2)
                                .build()
                ))
                .build();
    }

    private BillCalculationResponse validCalculateResponse() {
        return validCalculateResponse("bill-1");
    }

    private BillCalculationResponse validCalculateResponse(String billId) {
        return BillCalculationResponse.builder()
                .billId(billId)
                .appliedDiscountType(DiscountTypeEnum.EMPLOYEE)
                .billAmount(new BigDecimal("400.00"))
                .discountAmount(new BigDecimal("110.00"))
                .netPayableAmount(new BigDecimal("290.00"))
                .build();
    }
}