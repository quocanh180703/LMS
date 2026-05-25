package com.example.nhom3_tt_.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.CartController;
import com.example.nhom3_tt_.dtos.response.CartItemResponse;
import com.example.nhom3_tt_.dtos.response.CartResponse;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.services.CartService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void getCartById_success() throws Exception {
        Long cartId = 1L;
        Long studentId = 1L;
        List<CartItemResponse> cartItems = List.of(new CartItemResponse(1L, 1L, cartId));
        CartResponse cartResponse = new CartResponse(cartId, studentId, cartItems);

        when(cartService.getCartById(cartId)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/carts/{cartId}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId))
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.cartItems[0].id").value(1))
                .andExpect(jsonPath("$.cartItems[0].courseId").value(1L));
    }

    @Test
    void getOrCreateCartByStudentId_success() throws Exception {
        Long studentId = 1L;
        List<CartItemResponse> cartItems = List.of(new CartItemResponse(1L, 1L, 1L));
        CartResponse cartResponse = new CartResponse(1L, studentId, cartItems);

        when(cartService.getOrCreateCartByStudentId(studentId))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/carts/student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.studentId").value(studentId));
    }

    @Test
    void getCartPrice_success_withCoupons() throws Exception {
        Long cartId = 1L;
        List<String> couponCodes = List.of("DISCOUNT10", "DISCOUNT20");

        when(cartService.calculatePriceWithCoupons(cartId, couponCodes)).thenReturn(80.0);

        mockMvc.perform(post("/api/v1/carts/{id}/get-price", cartId)
                        .param("couponCodes", "DISCOUNT10", "DISCOUNT20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(80.0));
    }

    @Test
    void getCartPrice_success_withoutCoupons() throws Exception {
        Long cartId = 1L;

        when(cartService.calculatePriceWithCoupons(cartId, null)).thenReturn(100.0);

        mockMvc.perform(post("/api/v1/carts/{id}/get-price", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(100.0))
                .andExpect(jsonPath("$.appliedCoupons").value(""));
    }

//    @Test
//    void testGetCartPrice_DuplicateCoupons() throws Exception {
//        mockMvc.perform(post("/api/v1/carts/1/get-price")
//                        .param("couponCodes", "COUPON1", "COUPON1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())  // Kiểm tra mã trạng thái HTTP là 400
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CustomException))  // Kiểm tra ngoại lệ là CustomException
//                .andExpect(result -> assertEquals("Duplicate coupon codes are not allowed",  // Kiểm tra thông điệp lỗi
//                        result.getResolvedException().getMessage()))
//                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))  // Kiểm tra mã trạng thái HTTP trong phản hồi
//                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))  // Kiểm tra loại lỗi
//                .andExpect(jsonPath("$.message").value("Duplicate coupon codes are not allowed"));  // Kiểm tra thông điệp lỗi
//    }
}
