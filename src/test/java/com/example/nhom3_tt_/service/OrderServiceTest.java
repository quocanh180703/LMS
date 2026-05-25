package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.requests.OrderDetailRequest;
import com.example.nhom3_tt_.dtos.response.*;
import com.example.nhom3_tt_.dtos.response.earningAnalytic.EarningAnalyticResponse;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.ErrorCode;
import com.example.nhom3_tt_.mappers.EarningMapper;
import com.example.nhom3_tt_.mappers.OrderDetailMapper;
import com.example.nhom3_tt_.mappers.OrderMapper;
import com.example.nhom3_tt_.models.*;
import com.example.nhom3_tt_.repositories.*;
import com.example.nhom3_tt_.services.CouponService;
import com.example.nhom3_tt_.services.impl.OrderServiceImpl;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @InjectMocks
  private OrderServiceImpl orderService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderDetailRepository orderDetailRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private OrderDetailMapper orderDetailMapper;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private CouponService couponService;

  @Mock
  private CartItemRepository cartItemRepository;

  @Mock
  private EnrollRepository enrollRepository;

  @Mock
  private EarningMapper earningMapper;

    @Test
    void testCreateOrder_Success() {
        // Create test data
        User user = new User();
        user.setId(1L);

        String listProduct = "1,2";
        String vnp_Amount = "300.0";

        Course course1 = new Course();
        course1.setId(1L);
        course1.setRegularPrice(100.0);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setRegularPrice(200.0);

        CartItem cartItem1 = new CartItem();
        cartItem1.setCourse(course1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setCourse(course2);

        // Use mutable list to avoid UnsupportedOperationException
        List<CartItem> cartItems = new ArrayList<>(Arrays.asList(cartItem1, cartItem2));
        Cart cart = new Cart();
        cart.setCartItems(cartItems);

        // Mock repository calls
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
        // Change here: mock `findByCourseAndCart` instead of `findByCourse`
        when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.of(cartItem1));
        when(cartItemRepository.findByCourseAndCart(course2, cart)).thenReturn(Optional.of(cartItem2));
        when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(new OrderResponse());

        // Call the service method
        OrderResponse result = orderService.createOrder(user, vnp_Amount, listProduct);

        // Verify and assert results
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(enrollRepository, times(2)).save(any(Enroll.class));
    }

  @Test
  void testCreateOrder_ValidInputs() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Collections.emptyList();

    Course course1 = new Course();
    course1.setId(1L);
    course1.setRegularPrice(100.0);

    Course course2 = new Course();
    course2.setId(2L);
    course2.setRegularPrice(200.0);

    CartItem cartItem1 = new CartItem();
    cartItem1.setCourse(course1);

    CartItem cartItem2 = new CartItem();
    cartItem2.setCourse(course2);

    Cart cart = new Cart();
    cart.setCartItems(Arrays.asList(cartItem1, cartItem2));

    // Mock repository calls
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
    when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
    // Change here: mock `findByCourseAndCart` instead of `findByCourse`
    when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.of(cartItem1));
    when(cartItemRepository.findByCourseAndCart(course2, cart)).thenReturn(Optional.of(cartItem2));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));

    String result = orderService.getAmount(user, couponCodes, listProduct);

    assertEquals("300", result);
  }

  @Test
  void testGetAmount_ValidInputs_WithCoupons() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Arrays.asList("DISCOUNT10");

    Course course1 = new Course();
    course1.setId(1L);
    course1.setRegularPrice(100.0);

    Course course2 = new Course();
    course2.setId(2L);
    course2.setRegularPrice(200.0);

    CartItem cartItem1 = new CartItem();
    cartItem1.setCourse(course1);

    CartItem cartItem2 = new CartItem();
    cartItem2.setCourse(course2);

    Cart cart = new Cart();
    cart.setCartItems(Arrays.asList(cartItem1, cartItem2));

    CouponResponse coupon = new CouponResponse();
    coupon.setPercentDiscount(10.0);

    // Mock repository calls
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
    when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
    // Change here: mock `findByCourseAndCart` instead of `findByCourse`
    when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.of(cartItem1));
    when(cartItemRepository.findByCourseAndCart(course2, cart)).thenReturn(Optional.of(cartItem2));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));
    when(couponService.getByCode("DISCOUNT10")).thenReturn(coupon);

    String result = orderService.getAmount(user, couponCodes, listProduct);

    assertEquals("270", result);
  }


  @Test
  void testCreateOrder_CartEmpty() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    String vnp_Amount = "300.0";

    Cart cart = new Cart();
    cart.setCartItems(Collections.emptyList()); // Simulate an empty cart

    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));

    AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(user, vnp_Amount, listProduct));

    assertEquals(ErrorCode.CART_ITEM_EMPTY, exception.getErrorCode());
  }
  @Test
  void testCreateOrder_StudentNotFound() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    String vnp_Amount = "300.0";

    Course course1 = new Course();
    course1.setId(1L);
    course1.setRegularPrice(100.0);

    Course course2 = new Course();
    course2.setId(2L);
    course2.setRegularPrice(200.0);

    CartItem cartItem1 = new CartItem();
    cartItem1.setCourse(course1);

    CartItem cartItem2 = new CartItem();
    cartItem2.setCourse(course2);

    Cart cart = new Cart();
    cart.setCartItems(Arrays.asList(cartItem1, cartItem2));

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
    when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
    // Update here: `findByCourseAndCart` is used instead of `findByCourse`
    when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.of(cartItem1));
    when(cartItemRepository.findByCourseAndCart(course2, cart)).thenReturn(Optional.of(cartItem2));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));
    when(userRepository.findById(1L)).thenReturn(Optional.empty()); // User not found

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(user, vnp_Amount, listProduct));

    assertEquals("Student not found", exception.getMessage());
  }
  @Test
  void testGetOrderById_OrderFound() {
    Long orderId = 1L;
    Order order = new Order();
    OrderResponse orderResponse = new OrderResponse();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

    OrderResponse result = orderService.getOrderById(orderId);

    assertNotNull(result);
    assertEquals(orderResponse, result);
    verify(orderRepository).findById(orderId);
    verify(orderMapper).toOrderResponse(order);
  }

  @Test
  void testGetAmount_CourseNotFound() {
    User user = new User();
    Cart cart = new Cart();
    cart.setId(1L);
    cart.setCartItems(List.of(new CartItem()));
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Collections.emptyList();

    // Tìm course theo ID, nhưng không tìm thấy (return Optional.empty)
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());
    when(cartRepository.findByStudentId(user.getId())).thenReturn(Optional.of(cart));

    var exception = assertThrows(AppException.class, () -> orderService.getAmount(user, couponCodes, listProduct));

    assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
  }
  @Test
  void testGetAmount_CartItemNotFound() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Collections.emptyList();

    Course course1 = new Course();
    course1.setId(1L);

    Cart cart = new Cart();
    cart.setId(1L);
    CartItem cartItem = new CartItem();
    cartItem.setCourse(course1);
    cart.setCartItems(List.of(cartItem));

    when(cartRepository.findByStudentId(user.getId())).thenReturn(Optional.of(cart));
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
    // Sử dụng findByCourseAndCart thay vì findByCourse
    when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.getAmount(user, couponCodes, listProduct));

    assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
  }


  @Test
  void testGetAmount_CartEmpty() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Collections.emptyList();

    Cart cart = new Cart();
    cart.setCartItems(Collections.emptyList());

    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));

    AppException exception = assertThrows(AppException.class, () -> orderService.getAmount(user, couponCodes, listProduct));

    assertEquals(ErrorCode.CART_ITEM_EMPTY, exception.getErrorCode());
  }
  @Test
  void testGetAmount_ValidInputs_MultipleCoupons() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    List<String> couponCodes = Arrays.asList("DISCOUNT10", "DISCOUNT20");

    Course course1 = new Course();
    course1.setId(1L);
    course1.setRegularPrice(100.0);

    Course course2 = new Course();
    course2.setId(2L);
    course2.setRegularPrice(200.0);

    CartItem cartItem1 = new CartItem();
    cartItem1.setCourse(course1);

    CartItem cartItem2 = new CartItem();
    cartItem2.setCourse(course2);

    Cart cart = new Cart();
    cart.setCartItems(Arrays.asList(cartItem1, cartItem2));

    CouponResponse coupon1 = new CouponResponse();
    coupon1.setPercentDiscount(10.0);

    CouponResponse coupon2 = new CouponResponse();
    coupon2.setPercentDiscount(20.0);

    // Thay đổi findByCourse thành findByCourseAndCart
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
    when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
    when(cartItemRepository.findByCourseAndCart(course1, cart)).thenReturn(Optional.of(cartItem1));
    when(cartItemRepository.findByCourseAndCart(course2, cart)).thenReturn(Optional.of(cartItem2));
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));
    when(couponService.getByCode("DISCOUNT10")).thenReturn(coupon1);
    when(couponService.getByCode("DISCOUNT20")).thenReturn(coupon2);

    String result = orderService.getAmount(user, couponCodes, listProduct);

    // Assuming the coupons are cumulative: (100 + 200) * 0.7 = 210
    assertEquals("216", result);
  }


  @Test
  void testCreateOrder_CourseNotFound() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    String vnp_Amount = "300.0";

    // Mock a valid cart for the user
    Cart cart = new Cart();
    cart.setId(1L);
    cart.setCartItems(List.of(new CartItem())); // Cart items are irrelevant for this test

    when(cartRepository.findByStudentId(user.getId())).thenReturn(Optional.of(cart));
    when(courseRepository.findById(1L)).thenReturn(Optional.empty()); // Simulate missing course

    // Assert the expected exception
    AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(user, vnp_Amount, listProduct));

    assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
  }
  @Test
  void testCreateOrder_CartItemNotFound() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    String vnp_Amount = "300.0";

    Course course = new Course();
    course.setId(1L);

    CartItem cartItem = new CartItem();
    cartItem.setCourse(course);

    Cart cart = new Cart();
    cart.setCartItems(List.of(cartItem));

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

    // Thay đổi findByCourse thành findByCourseAndCart
    when(cartItemRepository.findByCourseAndCart(course, cart)).thenReturn(Optional.empty());
    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.of(cart));

    var exception = assertThrows(AppException.class, () -> orderService.createOrder(user, vnp_Amount, listProduct));

    assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
  }


  @Test
  void testCreateOrder_CartNotFound() {
    User user = new User();
    user.setId(1L);
    String listProduct = "1,2";
    String vnp_Amount = "300.0";

    when(cartRepository.findByStudentId(1L)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(user, vnp_Amount, listProduct));

    assertEquals(ErrorCode.CART_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void testGetOrderById_OrderNotFound() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(orderId));

    assertEquals("Order not found", exception.getMessage());
  }

  @Test
  void testGetOrderEntityById_OrderFound() {
    Long orderId = 1L;
    Order order = new Order();
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    Order result = orderService.getOrderEntityById(orderId);

    assertNotNull(result);
    verify(orderRepository, times(1)).findById(orderId);
  }

  @Test
  void testGetOrderEntityById_OrderNotFound() {
    Long orderId = 1L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderEntityById(orderId));

    assertEquals("Order not found", exception.getMessage());
    verify(orderRepository, times(1)).findById(orderId);
  }

  @Test
  void testGetAllOrdersByStudentId_OrdersFound() {
    Long studentId = 1L;
    User student = new User();
    Order order = new Order();
    List<Order> orders = Collections.singletonList(order);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(orderRepository.findByStudent_Id(studentId)).thenReturn(orders);

    List<OrderResponse> result = orderService.getAllOrdersByStudentId(studentId);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(userRepository, times(1)).findById(studentId);
    verify(orderRepository, times(1)).findByStudent_Id(studentId);
  }

  @Test
  void testGetAllOrdersByStudentId_StudentNotFound() {
    Long studentId = 1L;
    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getAllOrdersByStudentId(studentId));

    assertEquals("Student not found", exception.getMessage());
    verify(userRepository, times(1)).findById(studentId);
  }

  @Test
  void testGetAllOrdersByStudentId_NoOrdersFound() {
    Long studentId = 1L;
    User student = new User();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(orderRepository.findByStudent_Id(studentId)).thenReturn(Collections.emptyList());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderService.getAllOrdersByStudentId(studentId));

    assertEquals("No orders found for student with ID: " + studentId, exception.getMessage());
    verify(userRepository, times(1)).findById(studentId);
    verify(orderRepository, times(1)).findByStudent_Id(studentId);
  }

  @Test
  void testAddOrderDetailSuccess() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);
    Order order = new Order();
    order.setTotalAmount(0.0);
    Course course = new Course();
    OrderDetail orderDetail = new OrderDetail();
    orderDetail.setPrice(50.0);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(orderDetail);
    when(orderDetailMapper.convertToResponse(orderDetail)).thenReturn(new OrderDetailResponse());

    OrderDetailResponse result = orderService.addOrderDetail(orderDetailRequest);

    assertNotNull(result);
    verify(orderDetailRepository, times(1)).save(any(OrderDetail.class));
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void testAddOrderDetailOrderNotFound() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);

    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.addOrderDetail(orderDetailRequest));

    assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    verify(orderRepository, times(1)).findById(1L);
  }

  @Test
  void testAddOrderDetailCourseNotFound() {
    OrderDetailRequest orderDetailRequest = new OrderDetailRequest(1L, 1L);
    Order order = new Order();

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.addOrderDetail(orderDetailRequest));

    assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    verify(orderRepository, times(1)).findById(1L);
    verify(courseRepository, times(1)).findById(1L);
  }

  @Test
  void testDeleteOrderByOrderIdSuccess() {
    Order order = new Order();
    OrderDetail orderDetail1 = new OrderDetail();
    OrderDetail orderDetail2 = new OrderDetail();
    order.setOrderDetails(Arrays.asList(orderDetail1, orderDetail2));

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.deleteOrderByOrderId(1L);

    verify(orderRepository, times(1)).delete(order);
    verify(orderDetailRepository, times(1)).deleteAll(order.getOrderDetails());
  }

  @Test
  void testDeleteOrderByOrderIdNotFound() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.deleteOrderByOrderId(1L));

    assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    verify(orderRepository, times(1)).findById(1L);
  }

  @Test
  void testDeleteOrderDetailById_Success() {
    Long orderDetailId = 1L;
    Order order = new Order();
    order.setTotalAmount(100.0);
    OrderDetail orderDetail = new OrderDetail();
    orderDetail.setId(orderDetailId);
    orderDetail.setOrder(order);
    orderDetail.setPrice(50.0);

    when(orderDetailRepository.findById(orderDetailId)).thenReturn(Optional.of(orderDetail));

    orderService.deleteOrderDetailById(orderDetailId);

    verify(orderDetailRepository, times(1)).delete(orderDetail);
    verify(orderRepository, times(1)).save(order);
    assertEquals(50.0, order.getTotalAmount());
  }

  @Test
  void testDeleteOrderDetailById_OrderDetailNotFound() {
    Long orderDetailId = 1L;

    when(orderDetailRepository.findById(orderDetailId)).thenReturn(Optional.empty());

    AppException exception = assertThrows(AppException.class, () -> orderService.deleteOrderDetailById(orderDetailId));

    assertEquals(ErrorCode.ORDER_DETAIL_NOT_FOUND, exception.getErrorCode());
    verify(orderDetailRepository, times(1)).findById(orderDetailId);
    verify(orderDetailRepository, never()).delete(any(OrderDetail.class));
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void testGetEarning_WithOrders() {
    // Arrange: Chuẩn bị dữ liệu giả để kiểm tra
    Order order1 = new Order(); // Giả sử đã có các thuộc tính cần thiết
    order1.setId(1L);  // Đảm bảo set ID cho Order
    Order order2 = new Order();
    order2.setId(2L);  // Đảm bảo set ID cho Order

    EarningAnalyticResponse response1 = new EarningAnalyticResponse(); // Giả sử đã có các thuộc tính cần thiết
    response1.setId(1L);
    EarningAnalyticResponse response2 = new EarningAnalyticResponse();
    response2.setId(2L);

    // Mô phỏng hành vi của orderRepository và earningMapper
    when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));
    when(earningMapper.toEarningAnalyticResponse(order1)).thenReturn(response1);
    when(earningMapper.toEarningAnalyticResponse(order2)).thenReturn(response2);

    // Act: Gọi phương thức cần test
    List<EarningAnalyticResponse> result = orderService.getEarning();

    // Assert: Kiểm tra kết quả trả về
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(response1.getId(), result.get(0).getId()); // Kiểm tra ID đúng
    assertEquals(response2.getId(), result.get(1).getId()); // Kiểm tra ID đúng

    // Xác nhận các phương thức mock đã được gọi đúng
    verify(orderRepository, times(1)).findAll();
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order1);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order2);
  }

  @Test
  void testGetEarning_NoOrders() {
    when(orderRepository.findAll()).thenReturn(Collections.emptyList());

    List<EarningAnalyticResponse> result = orderService.getEarning();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(orderRepository, times(1)).findAll();
    verify(earningMapper, never()).toEarningAnalyticResponse(any(Order.class));
  }

  @Test
  void testGetEarningTotal_WithOrders() {
    // Arrange
    Order order1 = new Order();
    order1.setId(1L);
    order1.setTotalAmount(100.0);

    Order order2 = new Order();
    order2.setId(2L);
    order2.setTotalAmount(200.0);

    EarningAnalyticResponse response1 = new EarningAnalyticResponse();
    response1.setTotalAmount(100.0);

    EarningAnalyticResponse response2 = new EarningAnalyticResponse();
    response2.setTotalAmount(200.0);

    when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));
    when(earningMapper.toEarningAnalyticResponse(order1)).thenReturn(response1);
    when(earningMapper.toEarningAnalyticResponse(order2)).thenReturn(response2);

    // Act
    Double totalEarnings = orderService.getEarningTotal();

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(300.0, totalEarnings);
    verify(orderRepository, times(1)).findAll();
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order1);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order2);
  }

  @Test
  void testGetEarningTotal_NoOrders() {
    // Arrange
    when(orderRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    Double totalEarnings = orderService.getEarningTotal();

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(0.0, totalEarnings);
    verify(orderRepository, times(1)).findAll();
    verify(earningMapper, never()).toEarningAnalyticResponse(any(Order.class));
  }

  @Test
  void testGetEarningByDay_WithOrders() {
    // Arrange
    String date = "2023-10-10";
    LocalDate localDate = LocalDate.parse(date);
    Order order1 = new Order();
    order1.setTotalAmount(100.0);
    Order order2 = new Order();
    order2.setTotalAmount(200.0);

    EarningAnalyticResponse response1 = new EarningAnalyticResponse();
    response1.setTotalAmount(100.0);
    EarningAnalyticResponse response2 = new EarningAnalyticResponse();
    response2.setTotalAmount(200.0);

    when(orderRepository.findAllByCreatedAt(localDate)).thenReturn(Arrays.asList(order1, order2));
    when(earningMapper.toEarningAnalyticResponse(order1)).thenReturn(response1);
    when(earningMapper.toEarningAnalyticResponse(order2)).thenReturn(response2);

    // Act
    Double totalEarnings = orderService.getEarningByDay(date);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(300.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAt(localDate);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order1);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order2);
  }

  @Test
  void testGetEarningByDay_NoOrders() {
    // Arrange
    String date = "2023-10-10";
    LocalDate localDate = LocalDate.parse(date);

    when(orderRepository.findAllByCreatedAt(localDate)).thenReturn(Collections.emptyList());

    // Act
    Double totalEarnings = orderService.getEarningByDay(date);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(0.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAt(localDate);
    verify(earningMapper, never()).toEarningAnalyticResponse(any(Order.class));
  }

  @Test
  void testGetEarningByMonth_WithOrders() {
    // Arrange
    String month = "2023-10";
    Order order1 = new Order();
    order1.setTotalAmount(100.0);
    Order order2 = new Order();
    order2.setTotalAmount(200.0);

    EarningAnalyticResponse response1 = new EarningAnalyticResponse();
    response1.setTotalAmount(100.0);
    EarningAnalyticResponse response2 = new EarningAnalyticResponse();
    response2.setTotalAmount(200.0);

    when(orderRepository.findAllByCreatedAtMonth(month)).thenReturn(Arrays.asList(order1, order2));
    when(earningMapper.toEarningAnalyticResponse(order1)).thenReturn(response1);
    when(earningMapper.toEarningAnalyticResponse(order2)).thenReturn(response2);

    // Act
    Double totalEarnings = orderService.getEarningByMonth(month);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(300.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAtMonth(month);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order1);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order2);
  }

  @Test
  void testGetEarningByMonth_NoOrders() {
    // Arrange
    String month = "2023-10";
    when(orderRepository.findAllByCreatedAtMonth(month)).thenReturn(Collections.emptyList());

    // Act
    Double totalEarnings = orderService.getEarningByMonth(month);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(0.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAtMonth(month);
    verify(earningMapper, never()).toEarningAnalyticResponse(any(Order.class));
  }

  @Test
  void testGetEarningByYear_WithOrders() {
    // Arrange
    String year = "2023";
    Order order1 = new Order();
    order1.setTotalAmount(100.0);
    Order order2 = new Order();
    order2.setTotalAmount(200.0);

    EarningAnalyticResponse response1 = new EarningAnalyticResponse();
    response1.setTotalAmount(100.0);
    EarningAnalyticResponse response2 = new EarningAnalyticResponse();
    response2.setTotalAmount(200.0);

    when(orderRepository.findAllByCreatedAtYear(year)).thenReturn(Arrays.asList(order1, order2));
    when(earningMapper.toEarningAnalyticResponse(order1)).thenReturn(response1);
    when(earningMapper.toEarningAnalyticResponse(order2)).thenReturn(response2);

    // Act
    Double totalEarnings = orderService.getEarningByYear(year);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(300.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAtYear(year);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order1);
    verify(earningMapper, times(1)).toEarningAnalyticResponse(order2);
  }

  @Test
  void testGetEarningByYear_NoOrders() {
    // Arrange
    String year = "2023";
    when(orderRepository.findAllByCreatedAtYear(year)).thenReturn(Collections.emptyList());

    // Act
    Double totalEarnings = orderService.getEarningByYear(year);

    // Assert
    assertNotNull(totalEarnings);
    assertEquals(0.0, totalEarnings);
    verify(orderRepository, times(1)).findAllByCreatedAtYear(year);
    verify(earningMapper, never()).toEarningAnalyticResponse(any(Order.class));
  }

  @Test
  void testConvertStringToLocalDate_ValidDate() {
    String date = "2023-10-10";
    LocalDate expectedDate = LocalDate.of(2023, 10, 10);
    LocalDate result = orderService.convertStringToLocalDate(date);
    assertEquals(expectedDate, result);
  }

  @Test
  void testConvertStringToLocalDate_InvalidDate() {
    String date = "2023-13-10";
    assertThrows(DateTimeParseException.class, () -> orderService.convertStringToLocalDate(date));
  }

  @Test
  void testConvertStringToLocalDate_EmptyString() {
    String date = "";
    assertThrows(DateTimeParseException.class, () -> orderService.convertStringToLocalDate(date));
  }

  @Test
  void testConvertStringToLocalDate_NullString() {
    String date = null;
    assertThrows(NullPointerException.class, () -> orderService.convertStringToLocalDate(date));
  }
}