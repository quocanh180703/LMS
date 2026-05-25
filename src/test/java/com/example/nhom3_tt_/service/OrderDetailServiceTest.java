package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.requests.OrderDetailRequest;
import com.example.nhom3_tt_.dtos.response.OrderDetailResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.OrderDetailMapper;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Order;
import com.example.nhom3_tt_.models.OrderDetail;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.OrderDetailRepository;
import com.example.nhom3_tt_.repositories.OrderRepository;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.impl.OrderDetailServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class OrderDetailServiceTest {

  @Mock private OrderDetailRepository orderDetailRepository;

  @Mock private OrderDetailMapper orderDetailMapper;

  @Mock private OrderRepository orderRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private OrderDetailServiceImpl orderDetailService;

  @Test
  void create_success() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);
    Order order = new Order();
    Course course = new Course();
    course.setRegularPrice(100.0);
    OrderDetail orderDetail = new OrderDetail();

    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(orderDetailMapper.convertToEntity(orderDetailRequest)).thenReturn(orderDetail);
    when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    OrderDetailResponse result = orderDetailService.create(orderDetailRequest);

    assertNotNull(result);
    assertEquals(orderDetailResponse, result);

    verify(orderRepository).findById(1L);
    verify(courseRepository).findById(1L);
    verify(orderDetailMapper).convertToEntity(orderDetailRequest);
    verify(orderDetailRepository).save(orderDetail);
  }

  @Test
  void create_orderNotFound_throwsException() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);

    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.create(orderDetailRequest));
    assertEquals("Order not found with id = 1", exception.getMessage());

    verify(orderRepository).findById(1L);
  }

  @Test
  void create_courseNotFound_throwsException() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);
    Order order = new Order();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.create(orderDetailRequest));
    assertEquals("Course not found with id=1", exception.getMessage());

    verify(orderRepository).findById(1L);
    verify(courseRepository).findById(1L);
  }

  @Test
  void getAll_success() {
    OrderDetail orderDetail = new OrderDetail();
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    Page<OrderDetail> page = new PageImpl<>(List.of(orderDetail));

    when(orderDetailRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    List<OrderDetailResponse> result = orderDetailService.getAll(PageRequest.of(0, 10));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(orderDetailResponse, result.get(0));

    verify(orderDetailRepository).findAll(PageRequest.of(0, 10));
    verify(orderDetailMapper).convertToResponse(orderDetail);
  }

  @Test
  void getAll_noResults_returnsEmptyList() {
    when(orderDetailRepository.findAll(PageRequest.of(0, 10))).thenReturn(Page.empty());

    List<OrderDetailResponse> result = orderDetailService.getAll(PageRequest.of(0, 10));

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getById_success() {
    OrderDetail orderDetail = new OrderDetail();
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(orderDetail));
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    OrderDetailResponse result = orderDetailService.getById(1L);

    assertNotNull(result);
    assertEquals(orderDetailResponse, result);

    verify(orderDetailRepository).findById(1L);
    verify(orderDetailMapper).convertToResponse(orderDetail);
  }

  @Test
  void getById_notFound_throwsException() {
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.getById(1L));
    assertEquals("OrderDetail not found with id=1", exception.getMessage());

    verify(orderDetailRepository).findById(1L);
  }

  @Test
  void getByStudentId_success() {
    // Arrange
    Long studentId = 1L;
    OrderDetail orderDetail = new OrderDetail();
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    when(userRepository.existsById(studentId)).thenReturn(true);
    when(orderDetailRepository.findByOrderStudentId(studentId)).thenReturn(List.of(orderDetail));
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    // Act
    List<OrderDetailResponse> result = orderDetailService.getByStudentId(studentId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(orderDetailResponse, result.get(0));

    verify(userRepository).existsById(studentId);
    verify(orderDetailRepository).findByOrderStudentId(studentId);
    verify(orderDetailMapper).convertToResponse(orderDetail);
  }

  @Test
  void getByStudentId_userNotFound_throwsException() {
    // Arrange
    Long studentId = 1L;

    when(userRepository.existsById(studentId)).thenReturn(false);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.getByStudentId(studentId));
    assertEquals("User cannot found with id = " + studentId, exception.getMessage());

    verify(userRepository).existsById(studentId);
  }

  @Test
  void getByOrderId_success() {
    // Arrange
    Long orderId = 1L;
    OrderDetail orderDetail = new OrderDetail();
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    when(orderRepository.existsById(orderId)).thenReturn(true);
    when(orderDetailRepository.findByOrderId(orderId)).thenReturn(List.of(orderDetail));
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    // Act
    List<OrderDetailResponse> result = orderDetailService.getByOrderId(orderId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(orderDetailResponse, result.get(0));

    verify(orderRepository).existsById(orderId);
    verify(orderDetailRepository).findByOrderId(orderId);
    verify(orderDetailMapper).convertToResponse(orderDetail);
  }

  @Test
  void getByOrderId_orderNotFound_throwsException() {
    // Arrange
    Long orderId = 1L;

    when(orderRepository.existsById(orderId)).thenReturn(false);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.getByOrderId(orderId));
    assertEquals("Order cannot found with id = " + orderId, exception.getMessage());

    verify(orderRepository).existsById(orderId);
  }

  @Test
  void myOrderDetails_noOrders_returnsEmptyList() {
    // Arrange
    Long userId = 1L;
    User currentUser = new User();
    currentUser.setId(userId);

    // Create a mock Authentication that returns a User object
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(currentUser);

    // Set up the SecurityContext with our mock Authentication
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    when(orderDetailRepository.findByOrderStudentId(userId)).thenReturn(List.of());

    // Act
    List<OrderDetailResponse> result = orderDetailService.myOrderDetails();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(orderDetailRepository).findByOrderStudentId(userId);
  }

  @Test
  void update_success() {
    OrderDetailRequest newOrderDetail = new OrderDetailRequest(1L, 1L);
    OrderDetail existingOrderDetail = new OrderDetail();
    Order order = new Order();
    Course course = new Course();
    course.setRegularPrice(100.0);
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(existingOrderDetail));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(orderDetailRepository.save(existingOrderDetail)).thenReturn(existingOrderDetail);
    when(orderDetailMapper.convertToResponse(existingOrderDetail)).thenReturn(orderDetailResponse);

    OrderDetailResponse result = orderDetailService.update(1L, newOrderDetail);

    assertNotNull(result);
    assertEquals(orderDetailResponse, result);

    verify(orderDetailRepository).findById(1L);
    verify(orderRepository).findById(1L);
    verify(courseRepository).findById(1L);
    verify(orderDetailRepository).save(existingOrderDetail);
  }

  @Test
  void update_notFound_throwsException() {
    OrderDetailRequest newOrderDetail = new OrderDetailRequest(1L, 1L);
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.update(1L, newOrderDetail));
    assertEquals("OrderDetail not found with id=1", exception.getMessage());

    verify(orderDetailRepository).findById(1L);
  }

  @Test
  void update_orderNotFound_throwsException() {
    OrderDetailRequest newOrderDetail = new OrderDetailRequest(1L, 1L);

    // Giả lập trường hợp không tìm thấy Order
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(new OrderDetail()));
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    // Kiểm tra nếu exception được ném ra khi không tìm thấy Order
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.update(1L, newOrderDetail));
    assertEquals("Order not found with id = 1", exception.getMessage());

    verify(orderDetailRepository).findById(1L);
    verify(orderRepository).findById(1L);
  }

  @Test
  void update_courseNotFound_throwsException() {
    OrderDetailRequest newOrderDetail = new OrderDetailRequest(1L, 1L);
    OrderDetail existingOrderDetail = new OrderDetail();
    Order order = new Order();

    // Giả lập trường hợp tìm thấy Order nhưng không tìm thấy Course
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(existingOrderDetail));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());

    // Kiểm tra nếu exception được ném ra khi không tìm thấy Course
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.update(1L, newOrderDetail));
    assertEquals("Course not found with id=1", exception.getMessage());

    verify(orderDetailRepository).findById(1L);
    verify(orderRepository).findById(1L);
    verify(courseRepository).findById(1L);
  }

  @Test
  void forceDelete_success() {
    OrderDetail orderDetail = new OrderDetail();
    OrderDetailResponse orderDetailResponse = new OrderDetailResponse();

    when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(orderDetail));
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(orderDetailResponse);

    OrderDetailResponse result = orderDetailService.forceDelete(1L);

    assertNotNull(result);
    assertEquals(orderDetailResponse, result);

    verify(orderDetailRepository).findById(1L);
    verify(orderDetailRepository).deleteById(1L);
  }

  @Test
  void forceDelete_notFound_throwsException() {
    when(orderDetailRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> orderDetailService.forceDelete(1L));
    assertEquals("OrderDetail not found with id=1", exception.getMessage());

    verify(orderDetailRepository).findById(1L);
  }
}
