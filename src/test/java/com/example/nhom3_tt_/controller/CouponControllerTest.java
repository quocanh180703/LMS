package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.CouponController;
import com.example.nhom3_tt_.dtos.requests.CouponRequest;
import com.example.nhom3_tt_.dtos.response.CouponResponse;
import com.example.nhom3_tt_.services.CouponService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CouponControllerTest {

  @Mock private CouponService couponService;

  @InjectMocks private CouponController couponController;

    private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(couponController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver()) // Hỗ trợ Pageable
            .build();
  }

  @Test
  void createCoupon_success() throws Exception {
    CouponResponse couponResponse = new CouponResponse();
    couponResponse.setId(1L);
    couponResponse.setCode("JAVA1213");
    couponResponse.setBeginDay(
        LocalDateTime.now().plusDays(1)); // Tương ứng với mẫu JSON
    couponResponse.setExpireDay(
        LocalDateTime.now().plusDays(2)); // Tương ứng với mẫu JSON
    couponResponse.setPercentDiscount(15.0);

    when(couponService.create(any(CouponRequest.class))).thenReturn(couponResponse);

    mockMvc
        .perform(
            post("/api/v1/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"code\":\"JAVA1213\","
                        + "\"beginDay\":\"2027-10-08T20:28:58.398Z\"," 
                        + "\"expireDay\":\"2027-10-10T09:21:58.398Z\"," 
                        + "\"percentDiscount\":15}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("JAVA1213"))
        .andExpect(jsonPath("$.percentDiscount").value(15.0));
  }

  @Test
  void deleteForce_coupon_success() throws Exception {
    // Mô phỏng hành vi của service
    when(couponService.forceDelte(eq(1L))).thenReturn("Force delete Coupon successfully with id=1");

    mockMvc
        .perform(delete("/api/v1/coupons/force-delete/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Force delete Coupon successfully with id=1"));
  }

  @Test
  void getAll_success() throws Exception {
    // Tạo danh sách CouponResponse giả lập
    CouponResponse couponResponse1 = new CouponResponse();
    couponResponse1.setId(1L);
    couponResponse1.setCode("COUPON123");
    couponResponse1.setBeginDay(LocalDateTime.now());
    couponResponse1.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    couponResponse1.setPercentDiscount(10.0);

    CouponResponse couponResponse2 = new CouponResponse();
    couponResponse2.setId(2L);
    couponResponse2.setCode("COUPON456");
    couponResponse2.setBeginDay(LocalDateTime.now());
    couponResponse2.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later
    couponResponse2.setPercentDiscount(15.0);

    // Mô phỏng hành vi của service để trả về danh sách coupon
    when(couponService.getAll(any(Pageable.class)))
        .thenReturn(List.of(couponResponse1, couponResponse2));

    mockMvc
        .perform(get("/api/v1/coupons").queryParam("page", "0").queryParam("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2)) // Kiểm tra số lượng coupon trong danh sách
        .andExpect(jsonPath("$[0].code").value("COUPON123"))
        .andExpect(jsonPath("$[1].code").value("COUPON456"));
  }

  @Test
  void getUnexpireds_success() throws Exception {
    CouponResponse couponResponse = new CouponResponse();
    couponResponse.setId(1L);
    couponResponse.setCode("COUPON123");
    couponResponse.setBeginDay(LocalDateTime.now());
    couponResponse.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later
    couponResponse.setPercentDiscount(10.0);

    when(couponService.getUnexpireds(any())).thenReturn(List.of(couponResponse));

    mockMvc
        .perform(get("/api/v1/coupons/unexpireds").queryParam("page", "0").queryParam("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].code").value("COUPON123"));
  }

  @Test
  void getById_success() throws Exception {
    CouponResponse couponResponse = new CouponResponse();
    couponResponse.setId(1L);
    couponResponse.setCode("COUPON123");
    couponResponse.setBeginDay(LocalDateTime.now());
    couponResponse.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later
    couponResponse.setPercentDiscount(10.0);

    when(couponService.getById(1L)).thenReturn(couponResponse);

    mockMvc
        .perform(get("/api/v1/coupons/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.code").value("COUPON123"));
  }

  @Test
  void getByCode_success() throws Exception {
    CouponResponse couponResponse = new CouponResponse();
    couponResponse.setId(1L);
    couponResponse.setCode("COUPON123");
    couponResponse.setBeginDay(LocalDateTime.now());
    couponResponse.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later
    couponResponse.setPercentDiscount(10.0);

    when(couponService.getByCode("COUPON123")).thenReturn(couponResponse);

    mockMvc
        .perform(get("/api/v1/coupons/code/COUPON123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("COUPON123"));
  }

  @Test
  void updateCoupon_success() throws Exception {
    CouponResponse updatedCoupon = new CouponResponse();
    updatedCoupon.setId(1L);
    updatedCoupon.setCode("UPDATED123");
    updatedCoupon.setBeginDay(LocalDateTime.now().plusDays(1));
    updatedCoupon.setExpireDay(LocalDateTime.now().plusDays(10));
    updatedCoupon.setPercentDiscount(15.0);

    when(couponService.update(eq(1L), any(CouponRequest.class))).thenReturn(updatedCoupon);

    mockMvc
        .perform(
            put("/api/v1/coupons/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{"
                        + "\"code\":\"UPDATED123\","
                        + "\"beginDay\":\"2027-10-06T00:00:00\"," 
                        + "\"expireDay\":\"2027-10-15T00:00:00\"," 
                        + "\"percentDiscount\":15.0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("UPDATED123"))
        .andExpect(jsonPath("$.percentDiscount").value(15.0));
  }
}
