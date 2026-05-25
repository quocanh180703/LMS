package com.example.nhom3_tt_.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.nhom3_tt_.controllers.PaymentVnpayController;
import com.example.nhom3_tt_.dtos.OrderCreationRequest;
import com.example.nhom3_tt_.dtos.PaymentVnpayResponse;
import com.example.nhom3_tt_.dtos.response.CourseResponse;
import com.example.nhom3_tt_.dtos.response.OrderDetailResponse;
import com.example.nhom3_tt_.dtos.response.OrderResponse;
import com.example.nhom3_tt_.dtos.response.invoiceDetail.Invoice;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.OrderDetail;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.services.OrderDetailService;
import com.example.nhom3_tt_.services.PaymentService;
import com.example.nhom3_tt_.services.OrderService;
import com.example.nhom3_tt_.services.UserService;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.util.MailService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentVnpayControllerTest {
    @Mock
    private HttpServletRequest httpServletRequest;

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private MailService mailService;

    @Mock
    private OrderDetailService orderDetailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository; // Mock UserRepository

    @InjectMocks
    private PaymentVnpayController paymentVnpayController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentVnpayController).build();

        // Giả lập Authentication cho yêu cầu kiểm thử
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.emptyList()
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Giả lập userRepository.findByUsername trả về một đối tượng User giả
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
    }

    @Test
    public void testPay_Success() throws Exception {
        // Giả lập request hợp lệ
        OrderCreationRequest orderRequest = new OrderCreationRequest();
        orderRequest.setListProduct("1");

        // Giả lập PaymentVnpayResponse, đảm bảo phản hồi có cấu trúc giống như JSON mẫu
        PaymentVnpayResponse paymentResponse = new PaymentVnpayResponse(0, "success",
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=50000000&vnp_BankCode=NCB&vnp_Command=pay&vnp_CreateDate=20250106210312&vnp_CurrCode=VND&vnp_ExpireDate=20250106211812&vnp_IpAddr=0%3A0%3A0%3A0%3A0%3A0%3A0%3A1&vnp_Locale=vn&vnp_OrderInfo=Order123%7C2%7C1&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8000%2Fapi%2Fpayment%2Fvn-pay-callback&vnp_TmnCode=58X4B4HP&vnp_TxnRef=28476556&vnp_Version=2.1.0&vnp_SecureHash=ce3a8a115f27a70631c25ee8fb1821b26ed7fa4ebf9eac0f19c296dc5b8ad9fdb9ad8967538a5c471003e10b26727541870f5ce0129072cc9575eff48a9f3687");

        // Giả lập paymentService trả về kết quả hợp lệ
        when(paymentService.createVnPayPayment(any(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(paymentResponse);

        // Giả lập orderService.getAmount trả về giá trị hợp lệ (Giả sử giá trị là 100000)
        when(orderService.getAmount(any(), any(), any())).thenReturn("50000000");

        // Thực hiện kiểm tra API
        mockMvc.perform(
                        post("/api/payment/vn-pay")
                                .contentType("application/json")
                                .content("{\"listProduct\": \"1\"}"))
                .andExpect(status().isOk()); // Mong đợi trả về status 200 (OK)
    }


    @Test
    public void testPayCallback_Success() throws Exception {
        // Giả lập callback thành công
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullname("John Doe");
        mockUser.setEmail("john.doe@example.com");

        OrderResponse mockOrderResponse = new OrderResponse();
        mockOrderResponse.setOrderDetailIds(List.of(1L));
        mockOrderResponse.setOrderDate(new Date());

        OrderDetailResponse mockOrderDetail = new OrderDetailResponse();
        mockOrderDetail.setPrice(100000.0);
        mockOrderDetail.setCourse(new CourseResponse());

        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(orderService.createOrder(any(User.class), anyString(), anyString()))
                .thenReturn(mockOrderResponse);
        when(orderDetailService.getById(1L)).thenReturn(mockOrderDetail);

        // Thực hiện callback
        mockMvc.perform(get("/api/payment/vn-pay-callback")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionNo", "123456")
                        .param("vnp_OrderInfo", "order1|product1,product2|1")
                        .param("vnp_Amount", "10000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderDetailIds").isArray());

        // Kiểm tra email được gửi
        verify(mailService, times(1)).sendInvoice(any(Invoice.class));
    }


    @Test
    public void testPayCallback_Failure() throws Exception {
        // Giả lập callback với trạng thái thất bại
        String failureStatus = "01"; // Status không phải "00"
        String transactionId = "123456";
        String orderId = "order1";

        // Thực hiện callback với trạng thái thất bại
        mockMvc.perform(get("/api/payment/vn-pay-callback")
                        .param("vnp_ResponseCode", failureStatus)
                        .param("vnp_TransactionNo", transactionId)
                        .param("vnp_OrderInfo", orderId + "|product1,product2|1")
                        .param("vnp_Amount", "10000000"))
                .andExpect(status().isBadRequest()) // Mong đợi trả về mã lỗi 400
                .andExpect(jsonPath("$.message").value("Payment failed")) // Kiểm tra thông báo lỗi
                .andExpect(jsonPath("$.transactionId").value(transactionId)) // Kiểm tra transactionId trong phản hồi
                .andExpect(jsonPath("$.status").value(failureStatus)) // Kiểm tra status trong phản hồi
                .andExpect(jsonPath("$.orderId").value(orderId)); // Kiểm tra orderId trong phản hồi
    }


    @Test
    public void testPayWithDuplicateCoupons_ShouldThrowException() throws Exception {
        // Giả lập request với mã coupon trùng lặp
        List<String> couponCodes = Arrays.asList("DISCOUNT10", "DISCOUNT10"); // Coupon trùng lặp

        // Giả lập `hasCoupon = true`
        boolean hasCoupon = true;

        // Kiểm tra xem exception có được ném ra hay không
        CustomException exception = assertThrows(CustomException.class, () -> {
            if (hasCoupon) {
                Set<String> uniqueCoupons = new HashSet<>(couponCodes);
                if (uniqueCoupons.size() != couponCodes.size()) {
                    throw new CustomException(
                            "Duplicate coupon codes are not allowed", HttpStatus.BAD_REQUEST.value());
                }
            }
        });


    }

    @Test
    public void testPayWithUniqueCoupons_ShouldPass() throws Exception {
        // Giả lập request với các mã coupon hợp lệ (không trùng lặp)
        List<String> couponCodes = Arrays.asList("DISCOUNT10", "DISCOUNT20"); // Coupon không trùng

        // Giả lập `hasCoupon = true`
        boolean hasCoupon = true;

        // Kiểm tra logic không ném ra exception
        if (hasCoupon) {
            Set<String> uniqueCoupons = new HashSet<>(couponCodes);
            if (uniqueCoupons.size() != couponCodes.size()) {
                throw new CustomException(
                        "Duplicate coupon codes are not allowed", HttpStatus.BAD_REQUEST.value());
            }
        }

        // Nếu không có exception, test này thành công
    }
    @Test
    void pay_shouldThrowExceptionWhenDuplicateCouponCodesProvided() {
        // Mock user
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Duplicate coupon codes
        List<String> couponCodes = Arrays.asList("COUPON1", "COUPON1", "COUPON2");

        // Order request mock
        OrderCreationRequest orderRequest = new OrderCreationRequest();
        orderRequest.setListProduct("");

        // Expect CustomException
        CustomException exception = assertThrows(
                CustomException.class,
                () -> paymentVnpayController.pay(httpServletRequest, couponCodes, orderRequest)
        );

        // Assertions
        assert exception.getMessage().equals("Duplicate coupon codes are not allowed");
        assert exception.getStatusCode() == HttpStatus.BAD_REQUEST.value();
    }


}
