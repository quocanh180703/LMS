package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.OrderDetailController;
import com.example.nhom3_tt_.dtos.response.CourseResponse;
import com.example.nhom3_tt_.dtos.response.OrderDetailResponse;
import com.example.nhom3_tt_.services.OrderDetailService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderDetailControllerTest {

    @Mock private OrderDetailService orderDetailService;

    @InjectMocks private OrderDetailController orderDetailController;

    @Autowired private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(orderDetailController)
                        .setCustomArgumentResolvers(
                                new PageableHandlerMethodArgumentResolver()) // Hỗ trợ Pageable
                        .build();
    }

    @Test
    void getAll_success() throws Exception {
        OrderDetailResponse orderDetailResponse1 = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);
        OrderDetailResponse orderDetailResponse2 = new OrderDetailResponse(2L, 2L, new CourseResponse(), 200.0);

        when(orderDetailService.getAll(any(Pageable.class)))
                .thenReturn(List.of(orderDetailResponse1, orderDetailResponse2));

        mockMvc
                .perform(get("/api/v1/orderDetails").queryParam("page", "0").queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Kiểm tra số lượng order detail trong danh sách
                .andExpect(jsonPath("$[0].orderId").value(1L))
                .andExpect(jsonPath("$[1].orderId").value(2L));
    }

    @Test
    void getById_success() throws Exception {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);

        when(orderDetailService.getById(1L)).thenReturn(orderDetailResponse);

        mockMvc
                .perform(get("/api/v1/orderDetails/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1L));
    }

    @Test
    void getByStudentId_success() throws Exception {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);

        when(orderDetailService.getByStudentId(1L)).thenReturn(List.of(orderDetailResponse));

        mockMvc
                .perform(get("/api/v1/orderDetails/student/1").queryParam("page", "0").queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    void myOrderDetails_success() throws Exception {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);

        when(orderDetailService.myOrderDetails()).thenReturn(List.of(orderDetailResponse));

        mockMvc
                .perform(get("/api/v1/orderDetails/my-orderDetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    void getByOrderId_success() throws Exception {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);

        when(orderDetailService.getByOrderId(1L)).thenReturn(List.of(orderDetailResponse));

        mockMvc
                .perform(get("/api/v1/orderDetails/order/1").queryParam("page", "0").queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    void forceDelete_success() throws Exception {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1L, new CourseResponse(), 100.0);

        when(orderDetailService.forceDelete(1L)).thenReturn(orderDetailResponse);

        mockMvc
                .perform(delete("/api/v1/orderDetails/force-delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1L));
    }
}
