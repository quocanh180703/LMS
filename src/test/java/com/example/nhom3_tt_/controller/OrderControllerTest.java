package com.example.nhom3_tt_.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.nhom3_tt_.controllers.OrderController;
import com.example.nhom3_tt_.dtos.response.OrderResponse;
import com.example.nhom3_tt_.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void getOrderById_success() throws Exception {
        Long orderId = 1L;
        Long studentId = 1L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .student(null) // Replace with appropriate student DTO if needed
                .orderDate(new Date())
                .totalAmount(100.0)
                .orderDetailIds(List.of(1L, 2L))
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.student").doesNotExist())  // Expecting student info in response if applicable
                .andExpect(jsonPath("$.orderDate").exists())
                .andExpect(jsonPath("$.totalAmount").value(100.0))
                .andExpect(jsonPath("$.orderDetailIds[0]").value(1L))
                .andExpect(jsonPath("$.orderDetailIds[1]").value(2L));
    }

    @Test
    void getAllOrdersByStudentId_success() throws Exception {
        Long studentId = 1L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(1L)
                .student(null) // Replace with appropriate student DTO if needed
                .orderDate(new Date())
                .totalAmount(150.0)
                .orderDetailIds(List.of(1L, 2L))
                .build();

        when(orderService.getAllOrdersByStudentId(studentId))
                .thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/orders/student/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].student").doesNotExist())  // Expecting student info in response if applicable
                .andExpect(jsonPath("$[0].orderDate").exists())
                .andExpect(jsonPath("$[0].totalAmount").value(150.0))
                .andExpect(jsonPath("$[0].orderDetailIds[0]").value(1L))
                .andExpect(jsonPath("$[0].orderDetailIds[1]").value(2L));
    }

    @Test
    void deleteOrderByOrderId_success() throws Exception {
        Long orderId = 1L;

        doNothing().when(orderService).deleteOrderByOrderId(orderId);

        mockMvc.perform(delete("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully for orderId: " + orderId));
    }

    @Test
    void deleteOrderDetailById_success() throws Exception {
        Long orderDetailId = 1L;

        doNothing().when(orderService).deleteOrderDetailById(orderDetailId);

        mockMvc.perform(delete("/api/orders/orderDetails/{orderDetailId}", orderDetailId))
                .andExpect(status().isOk())
                .andExpect(content().string("OrderDetail deleted successfully for orderDetailId: " + orderDetailId));
    }

//    @Test
//    void getOrderById_orderNotFound() throws Exception {
//        Long orderId = 999L;  // Một order ID không tồn tại
//
//        // Giả lập service ném exception khi không tìm thấy order
//        when(orderService.getOrderById(orderId)).thenThrow(new IllegalArgumentException("Order not found"));
//
//        // Thực hiện test và mong đợi phản hồi 404 Not Found với thông điệp lỗi
//        mockMvc.perform(get("/api/orders/{orderId}", orderId))
//                .andExpect(status().isNotFound())  // Mong đợi status 404 Not Found
//                .andExpect(content().string("Order not found"));  // Kiểm tra thông điệp lỗi
//    }
}
