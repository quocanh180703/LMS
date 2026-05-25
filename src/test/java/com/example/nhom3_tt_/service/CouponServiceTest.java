package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.requests.CouponRequest;
import com.example.nhom3_tt_.dtos.response.CouponResponse;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.CouponMapper;
import com.example.nhom3_tt_.models.Coupon;
import com.example.nhom3_tt_.repositories.CouponRepository;
import com.example.nhom3_tt_.services.impl.CouponServiceImpl;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

  @Mock private CouponRepository couponRepository;

  @Mock private CouponMapper couponMapper;

  @InjectMocks private CouponServiceImpl couponService;

  @Test
  void createCoupon_success() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("TESTCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    Coupon coupon = new Coupon();
    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findByCodeIgnoreCase("TESTCODE")).thenReturn(Optional.empty());
    when(couponMapper.convertToEntity(couponRequest)).thenReturn(coupon);
    when(couponRepository.save(coupon)).thenReturn(coupon);
    when(couponMapper.convertToResponse(coupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.create(couponRequest);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findByCodeIgnoreCase("TESTCODE");
    verify(couponRepository).save(coupon);
    verify(couponMapper).convertToEntity(couponRequest);
    verify(couponMapper).convertToResponse(coupon);
  }

  @Test
  void createCoupon_invalidDates_throwsException() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setExpireDay(LocalDateTime.now());
    couponRequest.setBeginDay(LocalDateTime.now().plusDays(1));

    CustomException exception =
        assertThrows(CustomException.class, () -> couponService.create(couponRequest));
    assertEquals("ExpiredDate cannot before BeginDate", exception.getMessage());
  }

  @Test
  void createCoupon_duplicateCode_throwsException() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("TESTCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    when(couponRepository.findByCodeIgnoreCase("TESTCODE")).thenReturn(Optional.of(new Coupon()));

    CustomException exception =
        assertThrows(CustomException.class, () -> couponService.create(couponRequest));
    assertEquals("Coupon code is already existed!", exception.getMessage());

    verify(couponRepository).findByCodeIgnoreCase("TESTCODE");
  }

  @Test
  void getAllCoupons_success() {
    PageRequest pageable = PageRequest.of(0, 10);
    Coupon coupon = new Coupon();
    CouponResponse couponResponse = new CouponResponse();
    Page<Coupon> page = new PageImpl<>(List.of(coupon));

    when(couponRepository.findAll(pageable)).thenReturn(page);
    when(couponMapper.convertToResponse(coupon)).thenReturn(couponResponse);

    List<CouponResponse> result = couponService.getAll(pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(couponResponse, result.get(0));

    verify(couponRepository).findAll(pageable);
    verify(couponMapper).convertToResponse(coupon);
  }

  @Test
  void updateCoupon_sameCode_noException() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    Coupon existingCoupon = new Coupon();
    existingCoupon.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1));

    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(existingCoupon));
    when(couponRepository.save(existingCoupon)).thenReturn(existingCoupon);
    when(couponMapper.convertToResponse(existingCoupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.update(1L, couponRequest);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findById(1L);
    verify(couponRepository, never())
        .findByCodeIgnoreCase("OLDCODE"); // No duplicate check required
    verify(couponRepository).save(existingCoupon);
    verify(couponMapper).convertToResponse(existingCoupon);
  }

  @Test
  void updateCoupon_withDateChanges_only() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1));

    Coupon existingCoupon = new Coupon();
    existingCoupon.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now().minusDays(1));
    couponRequest.setExpireDay(LocalDateTime.now());

    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(existingCoupon));
    when(couponRepository.save(existingCoupon)).thenReturn(existingCoupon);
    when(couponMapper.convertToResponse(existingCoupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.update(1L, couponRequest);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findById(1L);
    verify(couponRepository).save(existingCoupon);
    verify(couponMapper).convertToResponse(existingCoupon);
  }

  @Test
  void updateCoupon_noChanges() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    Coupon existingCoupon = new Coupon();
    existingCoupon.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1));

    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(existingCoupon));
    when(couponRepository.save(existingCoupon)).thenReturn(existingCoupon);
    when(couponMapper.convertToResponse(existingCoupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.update(1L, couponRequest);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findById(1L);
    verify(couponRepository, never())
        .findByCodeIgnoreCase("OLDCODE"); // No check for duplicate code
    verify(couponRepository).save(existingCoupon);
    verify(couponMapper).convertToResponse(existingCoupon);
  }

  @Test
  void updateCoupon_notFound_throwsException() {
    // Arrange: Mock repository behavior to return Optional.empty() for the coupon with the given ID
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("NEWCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    when(couponRepository.findById(1L)).thenReturn(Optional.empty()); // Simulating coupon not found

    // Act & Assert: Verify that NotFoundException is thrown
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> couponService.update(1L, couponRequest));

    assertEquals("Coupon not found with id=1", exception.getMessage()); // Verify exception message

    // Verify that the repository method was called
    verify(couponRepository).findById(1L);
    verify(couponRepository, never()).save(any(Coupon.class)); // Ensure save is not called
  }

  @Test
  void getUnexpiredCoupons_success() {
    Date now = new Date();
    Coupon coupon = new Coupon();
    coupon.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later
    CouponResponse couponResponse = new CouponResponse();

    PageRequest pageable = PageRequest.of(0, 10);
    Page<Coupon> page = new PageImpl<>(List.of(coupon));

    // Use ArgumentMatchers.any() for Date and PageRequest
    when(couponRepository.findCouponUnexpired(
            ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.any(PageRequest.class)))
        .thenReturn(page);
    when(couponMapper.convertToResponse(coupon)).thenReturn(couponResponse);

    List<CouponResponse> result = couponService.getUnexpireds(pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(couponResponse, result.get(0));

    verify(couponRepository)
        .findCouponUnexpired(
            ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.any(PageRequest.class));
    verify(couponMapper).convertToResponse(coupon);
  }

  @Test
  void getUnexpiredCoupons_empty() {
    // Để tránh lỗi khi so sánh đối số Date
    Date now = new Date();

    PageRequest pageable = PageRequest.of(0, 10);
    Page<Coupon> page = new PageImpl<>(List.of()); // Empty page

    // Sử dụng ArgumentMatchers.any(Date.class) để khớp với bất kỳ đối tượng Date nào
    when(couponRepository.findCouponUnexpired(
            ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.any(PageRequest.class)))
        .thenReturn(page);

    List<CouponResponse> result = couponService.getUnexpireds(pageable);

    assertNotNull(result);
    assertTrue(result.isEmpty());

    // Kiểm tra lại phương thức được gọi
    verify(couponRepository)
        .findCouponUnexpired(
            ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.any(PageRequest.class));
  }

  @Test
  void getById_success() {
    Coupon coupon = new Coupon();
    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
    when(couponMapper.convertToResponse(coupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.getById(1L);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findById(1L);
    verify(couponMapper).convertToResponse(coupon);
  }

  @Test
  void getById_notFound_throwsException() {
    when(couponRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> couponService.getById(1L));
    assertEquals("Coupon not found with id=1", exception.getMessage());

    verify(couponRepository).findById(1L);
  }

  @Test
  void getCouponByCode_success() {
    Coupon coupon = new Coupon();
    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findByCodeIgnoreCase("TESTCODE")).thenReturn(Optional.of(coupon));
    when(couponMapper.convertToResponse(coupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.getByCode("TESTCODE");

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findByCodeIgnoreCase("TESTCODE");
    verify(couponMapper).convertToResponse(coupon);
  }

  @Test
  void getCouponByCode_notFound_throwsException() {
    when(couponRepository.findByCodeIgnoreCase("INVALIDCODE")).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> couponService.getByCode("INVALIDCODE"));
    assertEquals("Coupon cannot found with code=INVALIDCODE", exception.getMessage());

    verify(couponRepository).findByCodeIgnoreCase("INVALIDCODE");
  }

  @Test
  void updateCoupon_success() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("NEWCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    Coupon existingCoupon = new Coupon();
    existingCoupon.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1));

    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(existingCoupon));
    when(couponRepository.findByCodeIgnoreCase("NEWCODE")).thenReturn(Optional.empty());
    when(couponRepository.save(existingCoupon)).thenReturn(existingCoupon);
    when(couponMapper.convertToResponse(existingCoupon)).thenReturn(couponResponse);

    CouponResponse result = couponService.update(1L, couponRequest);

    assertNotNull(result);
    assertEquals(couponResponse, result);

    verify(couponRepository).findById(1L);
    verify(couponRepository).findByCodeIgnoreCase("NEWCODE");
    verify(couponRepository).save(existingCoupon);
    verify(couponMapper).convertToResponse(existingCoupon);
  }

  @Test
  void updateCoupon_invalidDates_throwsException() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setExpireDay(LocalDateTime.now());
    couponRequest.setBeginDay(LocalDateTime.now().plusDays(1)); // today

    CustomException exception =
        assertThrows(CustomException.class, () -> couponService.update(1L, couponRequest));
    assertEquals("ExpiredDate cannot before BeginDate", exception.getMessage());
  }

  @Test
  void updateCoupon_duplicateCode_throwsException() {
    CouponRequest couponRequest = new CouponRequest();
    couponRequest.setCode("NEWCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1)); // 1 day later

    Coupon existingCoupon = new Coupon();
    existingCoupon.setCode("OLDCODE");
    couponRequest.setBeginDay(LocalDateTime.now());
    couponRequest.setExpireDay(LocalDateTime.now().plusDays(1));

    CouponResponse couponResponse = new CouponResponse();

    when(couponRepository.findById(1L)).thenReturn(Optional.of(existingCoupon));
    when(couponRepository.findByCodeIgnoreCase("NEWCODE"))
        .thenReturn(Optional.of(new Coupon())); // Code already exists

    CustomException exception =
        assertThrows(CustomException.class, () -> couponService.update(1L, couponRequest));
    assertEquals("Coupon code is already existed!", exception.getMessage());

    verify(couponRepository).findById(1L);
    verify(couponRepository).findByCodeIgnoreCase("NEWCODE");
  }

  @Test
  void deleteCoupon_success() {
    when(couponRepository.existsById(1L)).thenReturn(true);
    doNothing().when(couponRepository).deleteById(1L);

    String result = couponService.forceDelte(1L);

    assertEquals("Force delete Coupon successfully with id=1", result);

    verify(couponRepository).existsById(1L);
    verify(couponRepository).deleteById(1L);
  }

  @Test
  void deleteCoupon_notFound_throwsException() {
    when(couponRepository.existsById(1L)).thenReturn(false);

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> couponService.forceDelte(1L));

    assertEquals("Coupon nou found with id=1", exception.getMessage());

    verify(couponRepository).existsById(1L);
    verify(couponRepository, never()).deleteById(1L);
  }
}
