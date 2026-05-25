package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.config.payment.VNPAYConfig;
import com.example.nhom3_tt_.dtos.OrderCreationRequest;
import com.example.nhom3_tt_.dtos.PaymentVnpayResponse;
import com.example.nhom3_tt_.services.PaymentService;
import com.example.nhom3_tt_.services.impl.PaymentServiceImpl;
import com.example.nhom3_tt_.util.VNPayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private VNPAYConfig vnPayConfig; // Mock VNPayConfig để giả lập hành vi

    @Mock
    private HttpServletRequest mockRequest; // Mock HttpServletRequest để giả lập yêu cầu HTTP

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl ;// Lớp cần kiểm tra (service)

    @Test
    void createVnPayPayment_success() throws JsonProcessingException {
        // Arrange
        int amount = 100000;
        String bankCode = "NCB";
        String idStudent = "1";
        OrderCreationRequest orderRequest = new OrderCreationRequest();
        orderRequest.setListProduct ("1,2,3");

        Map<String, String> vnpParamsMap = new HashMap<>();

        // Giả lập hành vi của các dependency
        when(vnPayConfig.getVNPayConfig(orderRequest,idStudent)).thenReturn(vnpParamsMap);

        // Act
        PaymentVnpayResponse response = paymentServiceImpl.createVnPayPayment(
                mockRequest, amount, bankCode, idStudent, orderRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertEquals("success", response.getMessage());
    }

    @Test
    void createVnPayPayment_fail_invalidAmount() throws JsonProcessingException {
        // Arrange
        int amount = -1; // Giá trị không hợp lệ
        String bankCode = "NCB";
        String idStudent = "1";
        OrderCreationRequest orderRequest = new OrderCreationRequest();
        orderRequest.setListProduct ("1,2,3");

        Map<String, String> vnpParamsMap = new HashMap<>();

        // Giả lập hành vi của các dependency
        when(vnPayConfig.getVNPayConfig(orderRequest,idStudent)).thenReturn(vnpParamsMap);

        // Act
        PaymentVnpayResponse response = paymentServiceImpl.createVnPayPayment(
                mockRequest, amount, bankCode, idStudent, orderRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getCode()); // Test này sẽ fail nếu logic xử lý số tiền âm chưa chính xác
        assertEquals("success", response.getMessage());
    }


}
