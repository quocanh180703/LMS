package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.nhom3_tt_.dtos.requests.CartItemRequest;
import com.example.nhom3_tt_.dtos.response.CartItemResponse;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.mappers.CartItemMapper;
import com.example.nhom3_tt_.models.Cart;
import com.example.nhom3_tt_.models.CartItem;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Enroll;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CartItemRepository;
import com.example.nhom3_tt_.repositories.CartRepository;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.EnrollRepository;
import com.example.nhom3_tt_.services.impl.CartItemServiceImp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class CartItemServiceTest {

  @Mock
  private CartItemRepository cartItemRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private EnrollRepository enrollRepository;

  @Mock
  private CartItemMapper cartItemMapper;

  @InjectMocks
  private CartItemServiceImp cartItemService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Test
  void addCartItem_throwsExceptionWhenCartNotFound() {
    // Arrange
    User user = new User();
    user.setId(1L);

    CartItemRequest cartItemRequest = new CartItemRequest();
    cartItemRequest.setCourseId(1L);

    // Mock Security Context
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    // Mock repository
    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    CustomException exception = assertThrows(CustomException.class,
        () -> cartItemService.addCartItem(cartItemRequest));
    assertEquals("Cart not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(cartRepository, times(1)).findByStudentId(anyLong());
    verifyNoInteractions(courseRepository, cartItemRepository, cartItemMapper);
  }

  @Test
  void addCartItem_throwsExceptionWhenCourseNotFound() {
    // Arrange
    User user = new User();
    user.setId(1L);

    Cart mockCart = new Cart();
    mockCart.setId(1L);

    CartItemRequest cartItemRequest = new CartItemRequest();
    cartItemRequest.setCourseId(1L);

    // Mock Security Context
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    // Mock repository
    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.of(mockCart));
    when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

    // Act & Assert
    CustomException exception = assertThrows(CustomException.class,
        () -> cartItemService.addCartItem(cartItemRequest));
    assertEquals("Course not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(cartRepository, times(1)).findByStudentId(anyLong());
    verify(courseRepository, times(1)).findById(anyLong());
    verifyNoInteractions(cartItemRepository, cartItemMapper);
  }

  @Test
  void addCartItem_success() {
    // Arrange
    User user = new User();
    user.setId(1L);

    Cart mockCart = new Cart();
    mockCart.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(1L);

    CartItem mockCartItem = CartItem.builder()
        .id(1L)
        .cart(mockCart)
        .course(mockCourse)
        .build();

    CartItemRequest cartItemRequest = new CartItemRequest();
    cartItemRequest.setCourseId(1L);

    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.of(mockCart));
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(mockCourse));
    when(cartItemRepository.getCartItemByCartIdAndCourseId(anyLong(), anyLong())).thenReturn(null);
    when(enrollRepository.getEnrollByStudentAndCourseId(any(User.class),
        any(Course.class))).thenReturn(null);
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);
    when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(new CartItemResponse());

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    // Act
    CartItemResponse response = cartItemService.addCartItem(cartItemRequest);

    // Assert
    assertNotNull(response);
    verify(cartRepository, times(1)).findByStudentId(anyLong());
    verify(courseRepository, times(1)).findById(anyLong());
    verify(cartItemRepository, times(1)).getCartItemByCartIdAndCourseId(anyLong(), anyLong());
    verify(enrollRepository, times(1)).getEnrollByStudentAndCourseId(any(User.class),
        any(Course.class));
    verify(cartItemRepository, times(1)).save(any(CartItem.class));
    verify(cartItemMapper, times(1)).toCartItemResponse(any(CartItem.class));
  }

  @Test
  void addCartItem_throwsExceptionWhenCartItemAlreadyExists() {
    // Arrange
    User user = new User();
    user.setId(1L);

    Cart mockCart = new Cart();
    mockCart.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(1L);

    CartItem mockCartItem = CartItem.builder()
        .id(1L)
        .cart(mockCart)
        .course(mockCourse)
        .build();

    CartItemRequest cartItemRequest = new CartItemRequest();
    cartItemRequest.setCourseId(1L);

    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.of(mockCart));
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(mockCourse));
    when(cartItemRepository.getCartItemByCartIdAndCourseId(anyLong(), anyLong())).thenReturn(
        mockCartItem);

    // Mock Security Context
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    CustomException exception = assertThrows(CustomException.class,
        () -> cartItemService.addCartItem(cartItemRequest));
    assertEquals("This item is already exists in this cart", exception.getMessage());
  }

  @Test
  void addCartItem_throwsExceptionWhenItemAlreadyEnrolled() {
    // Arrange
    User user = new User();
    user.setId(1L);

    Cart mockCart = new Cart();
    mockCart.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(1L);

    CartItemRequest cartItemRequest = new CartItemRequest();
    cartItemRequest.setCourseId(1L);

    Enroll mockEnroll = new Enroll();

    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.of(mockCart));
    when(courseRepository.findById(anyLong())).thenReturn(Optional.of(mockCourse));
    when(cartItemRepository.getCartItemByCartIdAndCourseId(anyLong(), anyLong())).thenReturn(null);
    when(enrollRepository.getEnrollByStudentAndCourseId(any(User.class),
        any(Course.class))).thenReturn(mockEnroll);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    CustomException exception = assertThrows(CustomException.class,
        () -> cartItemService.addCartItem(cartItemRequest));
    assertEquals("This item is already enrolled by this user", exception.getMessage());
    assertEquals(409, exception.getStatusCode());

    verify(cartRepository, times(1)).findByStudentId(anyLong());
    verify(courseRepository, times(1)).findById(anyLong());
    verify(cartItemRepository, times(1)).getCartItemByCartIdAndCourseId(anyLong(), anyLong());
    verify(enrollRepository, times(1)).getEnrollByStudentAndCourseId(any(User.class),
        any(Course.class));
    verifyNoMoreInteractions(cartItemRepository, cartItemMapper);
  }

  @Test
  void getAllCartItemsByOwn_success() {
    // Arrange
    User mockUser = new User();
    mockUser.setId(1L);

    Cart mockCart = new Cart();
    mockCart.setId(1L);

    List<CartItem> mockCartItems = List.of(
        CartItem.builder().id(1L).cart(mockCart).build(),
        CartItem.builder().id(2L).cart(mockCart).build()
    );

    Pageable pageable = PageRequest.of(0, 10);

    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.of(mockCart));
    when(cartItemRepository.findAllCartItemsByCartId(anyLong(), eq(pageable))).thenReturn(
        mockCartItems);
    when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(new CartItemResponse());

    // Mock Security Context
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    // Act
    List<CartItemResponse> responses = cartItemService.getAllCartItemsByOwn(pageable);

    // Assert
    assertNotNull(responses);
    assertEquals(2, responses.size());
    verify(cartRepository, times(1)).findByStudentId(anyLong());
    verify(cartItemRepository, times(1)).findAllCartItemsByCartId(anyLong(), eq(pageable));
    verify(cartItemMapper, times(2)).toCartItemResponse(any(CartItem.class));
  }

  @Test
  void getAllCartItemsByOwn_throwsExceptionWhenCartNotFound() {
    // Arrange
    User mockUser = new User();
    mockUser.setId(1L);

    Pageable pageable = PageRequest.of(0, 10);

    when(cartRepository.findByStudentId(anyLong())).thenReturn(Optional.empty());

    // Mock Security Context
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    // Act & Assert
    CustomException exception = assertThrows(CustomException.class,
        () -> cartItemService.getAllCartItemsByOwn(pageable));
    assertEquals("Cart not found", exception.getMessage());
  }

  @Test
  void getAllCartItemsByCartId_success() {
    // Arrange
    Long cartId = 1L;

    Cart mockCart = new Cart();
    mockCart.setId(cartId);

    List<CartItem> mockCartItems = List.of(
        CartItem.builder().id(1L).cart(mockCart).build(),
        CartItem.builder().id(2L).cart(mockCart).build()
    );

    Pageable pageable = PageRequest.of(0, 10);

    when(cartItemRepository.findAllCartItemsByCartId(eq(cartId), eq(pageable))).thenReturn(
        mockCartItems);
    when(cartItemMapper.toCartItemResponse(any(CartItem.class))).thenReturn(new CartItemResponse());

    // Act
    List<CartItemResponse> responses = cartItemService.getAllCartItemsByCartId(cartId, pageable);

    // Assert
    assertNotNull(responses);
    assertEquals(2, responses.size());
    verify(cartItemRepository, times(1)).findAllCartItemsByCartId(eq(cartId), eq(pageable));
    verify(cartItemMapper, times(2)).toCartItemResponse(any(CartItem.class));
  }

  @Test
  void getAllCartItemsByCartId_emptyList() {
    // Arrange
    Long cartId = 1L;

    Pageable pageable = PageRequest.of(0, 10);

    when(cartItemRepository.findAllCartItemsByCartId(eq(cartId), eq(pageable))).thenReturn(
        List.of());

    // Act
    List<CartItemResponse> responses = cartItemService.getAllCartItemsByCartId(cartId, pageable);

    // Assert
    assertNotNull(responses);
    assertTrue(responses.isEmpty());
    verify(cartItemRepository, times(1)).findAllCartItemsByCartId(eq(cartId), eq(pageable));
    verify(cartItemMapper, times(0)).toCartItemResponse(any(CartItem.class));
  }


  @Test
  void getAll_CartItems_success() {
    Pageable pageable = Pageable.unpaged();
    CartItem cartItem = new CartItem();
    CartItemResponse cartItemResponse = new CartItemResponse();

    when(cartItemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(cartItem)));
    when(cartItemMapper.toCartItemResponse(cartItem)).thenReturn(cartItemResponse);

    List<CartItemResponse> result = cartItemService.getAllCartItems(pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(cartItemResponse, result.get(0));

    verify(cartItemRepository).findAll(pageable);
    verify(cartItemMapper).toCartItemResponse(cartItem);
  }

  @Test
  void testCreateCartItem_CartNotFound() {
    Long cartId = 1L;
    Long courseId = 2L;

    // Simulate cart not found
    when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(
        CustomException.class, () -> cartItemService.createCartItem(cartId, courseId));

    assertEquals("Cart not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(cartRepository).findById(cartId);
    verifyNoMoreInteractions(cartRepository, courseRepository, cartItemRepository);
  }

  @Test
  void testCreateCartItem_CourseNotFound() {
    Long cartId = 1L;
    Long courseId = 2L;
    Cart cart = new Cart();

    when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(
        CustomException.class, () -> cartItemService.createCartItem(cartId, courseId));

    assertEquals("Course not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(cartRepository).findById(cartId);
    verify(courseRepository).findById(courseId);
    verifyNoMoreInteractions(cartRepository, courseRepository, cartItemRepository);
  }

  @Test
  void create_CartItem_success() {
    Cart cart = new Cart();
    Course course = new Course();
    CartItem cartItem = CartItem.builder().course(course).cart(cart).build();
    CartItem savedCartItem = CartItem.builder().course(course).cart(cart).build();
    CartItemResponse cartItemResponse = new CartItemResponse();

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
    when(cartItemRepository.getCartItemByCartIdAndCourseId(1L, 2L)).thenReturn(null);
    when(cartItemRepository.save(cartItem)).thenReturn(savedCartItem);
    when(cartItemMapper.toCartItemResponse(savedCartItem)).thenReturn(cartItemResponse);

    CartItemResponse result = cartItemService.createCartItem(1L, 2L);

    assertNotNull(result);
    assertEquals(cartItemResponse, result);

    verify(cartRepository).findById(1L);
    verify(courseRepository).findById(2L);
    verify(cartItemRepository).getCartItemByCartIdAndCourseId(1L, 2L);
    verify(cartItemRepository).save(cartItem);
    verify(cartItemMapper).toCartItemResponse(cartItem);
  }

  @Test
  void testCreateCartItem_AlreadyExists() {
    Long cartId = 1L;
    Long courseId = 2L;
    Cart cart = new Cart();
    Course course = new Course();
    CartItem existingCartItem = new CartItem();

    when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(cartItemRepository.getCartItemByCartIdAndCourseId(cartId, courseId)).thenReturn(
        existingCartItem);

    CustomException exception = assertThrows(
        CustomException.class, () -> cartItemService.createCartItem(cartId, courseId));

    assertNotNull(exception);
    assertEquals("This item is already exists in this cart", exception.getMessage());
    verify(cartRepository).findById(cartId);
    verify(courseRepository).findById(courseId);
    verify(cartItemRepository).getCartItemByCartIdAndCourseId(cartId, courseId);
    verifyNoMoreInteractions(cartItemRepository);
  }

  @Test
  void delete_CartItem_success() {
    CartItem cartItem = new CartItem();
    CartItemResponse cartItemResponse = new CartItemResponse();

    when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
    when(cartItemMapper.toCartItemResponse(cartItem)).thenReturn(cartItemResponse);

    CartItemResponse result = cartItemService.deleteCartItem(1L);

    assertNotNull(result);
    assertEquals(cartItemResponse, result);

    verify(cartItemRepository).findById(1L);
    verify(cartItemMapper).toCartItemResponse(cartItem);
    verify(cartItemRepository).deleteById(1L);
  }

  @Test
  void testDeleteCartItem_NotFound() {
    Long cartItemId = 1L;

    when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(RuntimeException.class,
        () -> cartItemService.deleteCartItem(cartItemId));
    assertNotNull(exception);
    verify(cartItemRepository).findById(cartItemId);
  }
}