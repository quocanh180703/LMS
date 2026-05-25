package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.response.CartResponse;
import com.example.nhom3_tt_.dtos.response.CouponResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.CartMapper;
import com.example.nhom3_tt_.models.Cart;
import com.example.nhom3_tt_.models.CartItem;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CartRepository;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.CouponService;
import com.example.nhom3_tt_.services.impl.CartServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @InjectMocks private CartServiceImpl cartService;

  @Mock private CartRepository cartRepository;

  @Mock private UserRepository userRepository;

  @Mock private CartMapper cartMapper;

  @Mock private CouponService couponService;

  @Test
  void testGetAllCarts() {
    Cart cart = Cart.builder().id(1L).build();
    List<Cart> carts = List.of(cart);
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(cartRepository.findAll()).thenReturn(carts);
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    List<CartResponse> result = cartService.getAllCarts();

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
    verify(cartRepository, times(1)).findAll();
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testGetCartById() {
    Cart cart = Cart.builder().id(1L).build();
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    CartResponse result = cartService.getCartById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(cartRepository, times(1)).findById(1L);
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testGetCartByIdNotFound() {
    when(cartRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              cartService.getCartById(1L);
            });

    assertEquals("Cart not found", exception.getMessage());
    verify(cartRepository, times(1)).findById(1L);
  }

  @Test
  void testCreateCart() {
    User student = User.builder().id(1L).build();
    Cart cart = Cart.builder().id(1L).student(student).build();
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(student));
    when(cartRepository.save(any(Cart.class))).thenReturn(cart);
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    CartResponse result = cartService.createCart(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userRepository, times(1)).findById(1L);
    verify(cartRepository, times(1)).save(any(Cart.class));
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testDeleteCart() {
    Cart cart = Cart.builder().id(1L).build();
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    doNothing().when(cartRepository).delete(cart);
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    CartResponse result = cartService.deleteCart(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(cartRepository, times(1)).findById(1L);
    verify(cartRepository, times(1)).delete(cart);
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testGetOrCreateCartByStudentId_CartExists() {
    User student = User.builder().id(1L).build();
    Cart cart = Cart.builder().id(1L).student(student).build();
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(student));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    CartResponse result = cartService.getOrCreateCartByStudentId(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userRepository, times(1)).findById(1L);
    verify(cartRepository, times(1)).findByStudentId(1L);
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testGetOrCreateCartByStudentId_CartNotExists() {
    User student = User.builder().id(1L).build();
    Cart cart = Cart.builder().id(1L).student(student).build();
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(student));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class))).thenReturn(cart);
    when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

    CartResponse result = cartService.getOrCreateCartByStudentId(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userRepository, times(2)).findById(1L);
    verify(cartRepository, times(1)).findByStudentId(1L);
    verify(cartRepository, times(1)).save(any(Cart.class));
    verify(cartMapper, times(1)).toCartResponse(cart);
  }

  @Test
  void testCreateCart_StudentNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              cartService.createCart(1L);
            });

    assertEquals("Student not found", exception.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void testDeleteCart_CartNotFound() {
    when(cartRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              cartService.deleteCart(1L);
            });

    assertEquals("Cart not found", exception.getMessage());
    verify(cartRepository, times(1)).findById(1L);
    verify(cartRepository, never()).delete(any(Cart.class));
  }

  @Test
  void testGetOrCreateCartByStudentId_StudentNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              cartService.getOrCreateCartByStudentId(1L);
            });

    assertEquals("Student not found with id: 1", exception.getMessage());
    verify(userRepository, times(1)).findById(1L);
    verify(cartRepository, never()).findByStudentId(anyLong());
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void testGetOrCreateCartByStudentId_StudentNotFoundOnSecondFind() {
    User student = User.builder().id(1L).build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(student)).thenReturn(Optional.empty());

    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              cartService.getOrCreateCartByStudentId(1L);
            });

    assertEquals("Student not found with id: 1", exception.getMessage());
    verify(userRepository, times(2)).findById(1L); // Gọi 2 lần
    verify(cartRepository, times(1)).findByStudentId(1L);
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void testCalculatePriceWithCoupons_NoCoupons() {
    // Arrange
    Cart cart = new Cart();
    List<CartItem> cartItems = new ArrayList<>();

    CartItem item1 = new CartItem();
    Course course1 = new Course();
    course1.setRegularPrice(100.0);
    item1.setCourse(course1);
    cartItems.add(item1);

    CartItem item2 = new CartItem();
    Course course2 = new Course();
    course2.setRegularPrice(200.0);
    item2.setCourse(course2);
    cartItems.add(item2);

    cart.setCartItems(cartItems);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

    // Act
    double result = cartService.calculatePriceWithCoupons(1L, null);

    // Assert
    assertEquals(300.0, result);
    verify(cartRepository, times(1)).findById(1L);
    verify(couponService, never()).getByCode(anyString());
  }

  @Test
  void testCalculatePriceWithCoupons_WithValidCoupons() {
    // Arrange
    Cart cart = new Cart();
    List<CartItem> cartItems = new ArrayList<>();

    CartItem item1 = new CartItem();
    Course course1 = new Course();
    course1.setRegularPrice(100.0);
    item1.setCourse(course1);
    cartItems.add(item1);

    CartItem item2 = new CartItem();
    Course course2 = new Course();
    course2.setRegularPrice(200.0);
    item2.setCourse(course2);
    cartItems.add(item2);

    cart.setCartItems(cartItems);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

    CouponResponse coupon1 = new CouponResponse();
    coupon1.setPercentDiscount(10.0);
    when(couponService.getByCode("COUPON10")).thenReturn(coupon1);

    CouponResponse coupon2 = new CouponResponse();
    coupon2.setPercentDiscount(20.0);
    when(couponService.getByCode("COUPON20")).thenReturn(coupon2);

    List<String> coupons = List.of("COUPON10", "COUPON20");

    // Act
    double result = cartService.calculatePriceWithCoupons(1L, coupons);

    // Assert
    double expectedPrice = 300.0;
    expectedPrice -= expectedPrice * 0.1; // Apply 10% discount
    expectedPrice -= expectedPrice * 0.2; // Apply 20% discount
    assertEquals(expectedPrice, result);

    verify(cartRepository, times(1)).findById(1L);
    verify(couponService, times(1)).getByCode("COUPON10");
    verify(couponService, times(1)).getByCode("COUPON20");
  }

  @Test
  void testCalculatePriceWithCoupons_CartNotFound() {
    // Arrange
    when(cartRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception =
        assertThrows(
            NotFoundException.class,
            () -> cartService.calculatePriceWithCoupons(1L, List.of("COUPON10")));

    assertEquals("Cart not found with id = 1", exception.getMessage());
    verify(cartRepository, times(1)).findById(1L);
    verify(couponService, never()).getByCode(anyString());
  }
}
