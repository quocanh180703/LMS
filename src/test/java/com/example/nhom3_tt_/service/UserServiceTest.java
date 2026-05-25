package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import com.example.nhom3_tt_.dtos.requests.UserChangePasswordOTPRequest;
import com.example.nhom3_tt_.dtos.requests.UserChangePasswordRequest;
import com.example.nhom3_tt_.dtos.requests.UserConfirmOTPRequest;
import com.example.nhom3_tt_.dtos.requests.profile.EditProfileRequest;
import com.example.nhom3_tt_.dtos.response.profile.EditProfileResponse;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.ProfileMapper;
import com.example.nhom3_tt_.models.ETypeRole;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private ProfileMapper profileMapper;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserServiceImpl userService;

  private User user;
  private UserChangePasswordRequest userChangePasswordRequest;
  private UserConfirmOTPRequest userConfirmOTPRequest;
  private SecurityContext securityContext;
  private UserChangePasswordOTPRequest userChangePasswordOTPRequest;

  private Authentication authentication;

  @BeforeEach
  void initData() {
    user = new User();
    user.setId(1L);
    user.setUsername("huy_cr311");
    user.setEmail("amazingshadow.dev@gmail.com");
    user.setPassword(new BCryptPasswordEncoder().encode("123456789"));
    user.setOTP("123456");
    user.setOtpExpirationDate(
        new Date(System.currentTimeMillis() + 300000)); // OTP valid for 5 minutes

    authentication = mock(Authentication.class);
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    userChangePasswordRequest = new UserChangePasswordRequest();
    userChangePasswordRequest.setOld_password("123456789");
    userChangePasswordRequest.setNew_password("newpassword");
    userChangePasswordRequest.setConfirm_password("newpassword");

    userConfirmOTPRequest = new UserConfirmOTPRequest();
    userConfirmOTPRequest.setEmail("amazingshadow.dev@gmail.com");
    userConfirmOTPRequest.setOTP("123456");

    userChangePasswordOTPRequest = new UserChangePasswordOTPRequest();
    userChangePasswordOTPRequest.setEmail("amazingshadow.dev@gmail.com");
    userChangePasswordOTPRequest.setOtp("123456");
    userChangePasswordOTPRequest.setNewPassword("newpassword");
    userChangePasswordOTPRequest.setConfirmPassword("newpassword");
  }

  @Test
  void findByUsername_validRequest_success() {
    // GIVEN
    when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

    // WHEN
    Optional<User> result = userService.findByUsername(user.getUsername());

    // THEN
    assertTrue(result.isPresent());
    assertEquals(user.getUsername(), result.get().getUsername());
    verify(userRepository).findByUsername(user.getUsername());
  }

  @Test
  void save_validRequest_success() {
    // GIVEN
    when(userRepository.save(any())).thenReturn(user);

    // WHEN
    User savedUser = userService.save(user);

    // THEN
    assertNotNull(savedUser);
    assertEquals(user.getUsername(), savedUser.getUsername());
    verify(userRepository).save(user);
  }

  @Test
  void changePassword_validRequest_success() {
    // GIVEN
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("huy_cr311");
    when(userRepository.findByUsername("huy_cr311")).thenReturn(Optional.of(user));

    // WHEN
    String result = userService.changePassword(userChangePasswordRequest);

    // THEN
    assertEquals("Change password Successful", result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void changePassword_wrongOldPassword_fail() {
    // GIVEN
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("huy_cr311");
    when(userRepository.findByUsername("huy_cr311")).thenReturn(Optional.of(user));

    userChangePasswordRequest.setOld_password("wrongpassword");
    // WHEN, THEN
    assertThrows(AppException.class, () -> userService.changePassword(userChangePasswordRequest));
  }

  @Test
  void changePassword_passwordsNotEqual_fail() {
    // GIVEN
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("huy_cr311");
    when(userRepository.findByUsername("huy_cr311")).thenReturn(Optional.of(user));

    userChangePasswordRequest.setNew_password("newpassword");
    userChangePasswordRequest.setConfirm_password("newpassword1");

    // WHEN, THEN
    assertThrows(AppException.class, () -> userService.changePassword(userChangePasswordRequest));
  }

  @Test
  void confirmOTP_validRequest_success() {
    when(userRepository.findByEmail("amazingshadow.dev@gmail.com")).thenReturn(Optional.of(user));

    assertTrue(userService.confirmOtp(userConfirmOTPRequest));
  }

  @Test
  void confirmOTP_invalidOTP_fail() {
    when(userRepository.findByEmail("amazingshadow.dev@gmail.com")).thenReturn(Optional.of(user));

    userConfirmOTPRequest.setOTP("1234567");

    assertThrows(AppException.class, () -> userService.confirmOtp(userConfirmOTPRequest));
  }

  @Test
  void changePasswordOTP_validRequest_success() {
    when(userRepository.findByEmail("amazingshadow.dev@gmail.com")).thenReturn(Optional.of(user));

    String result = userService.changePasswordOTP(userChangePasswordOTPRequest);

    assertEquals("Change password Successful", result);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void changePasswordOTP_passwordsNotEqual_fail() {
    when(userRepository.findByEmail("amazingshadow.dev@gmail.com")).thenReturn(Optional.of(user));

    userChangePasswordOTPRequest.setNewPassword("newpassword");
    userChangePasswordOTPRequest.setConfirmPassword("differentpassword");

    assertThrows(
        AppException.class, () -> userService.changePasswordOTP(userChangePasswordOTPRequest));
  }

  @Test
  void getProfile_validRequest_success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(profileMapper.toProfileResponse(user)).thenReturn(new EditProfileResponse());

    EditProfileResponse response = userService.getProfile(1L);

    assertNotNull(response);
    verify(profileMapper).toProfileResponse(user);
  }

  @Test
  void getProfile_userNotFound_fail() {
    // GIVEN: Mô phỏng trường hợp không tìm thấy người dùng với ID = 2L
    Long userId = 2L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // WHEN, THEN: Kiểm tra rằng khi không tìm thấy người dùng, sẽ ném ra AppException
    assertThrows(AppException.class, () -> userService.getProfile(userId));
  }

  @Test
  void editProfile_validRequest_success() throws IOException {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(profileMapper.toProfileResponse(user)).thenReturn(new EditProfileResponse());

    EditProfileRequest request = new EditProfileRequest();
    EditProfileResponse response = userService.editProfile(request, null);

    assertNotNull(response);
    verify(profileMapper).updateProfile(eq(user), eq(request));
    verify(userRepository).save(user);
  }

  @Test
  void editProfile_userNotFound_throwsException() {
    // Arrange
    EditProfileRequest request = new EditProfileRequest();
    request.setFullname("Le Tan");
    request.setEmail("leetaan1902@example.com");

    // Giả lập user đăng nhập trong SecurityContext
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user); // `user` đại diện cho user đăng nhập
    SecurityContextHolder.setContext(securityContext);

    // Giả lập trường hợp user không tồn tại trong DB
    when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

    // Act & Assert
    AppException exception =
        assertThrows(AppException.class, () -> userService.editProfile(request, null));
    assertEquals("User not found", exception.getMessage());

    // Verify
    verify(userRepository).findById(user.getId()); // Đảm bảo `userRepository.findById` được gọi
    verify(profileMapper, never()).updateProfile(any(), any()); // Đảm bảo mapper không được gọi
    //    verify(repository, never()).save(any());
  }

  @Test
  void getUserById_validRequest_success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User result = userService.getUserById(1L);

    assertNotNull(result);
    assertEquals(user.getId(), result.getId());
  }

  @Test
  void getUserById_userNotFound_fail() {
    when(userRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(AppException.class, () -> userService.getUserById(2L));
  }

  @Test
  void blockUnblockUser_userBlocked_success() {
    // Arrange: Mô phỏng người dùng đã bị chặn (status = false)
    User user = new User();
    user.setId(1L);
    user.setStatus(false); // Người dùng đã bị chặn

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act: Thực hiện kích hoạt người dùng
    String result = userService.blockUnblockUser(1L);

    // Assert: Kiểm tra kết quả trả về đúng
    assertNotNull(result);
    assertEquals("User has been activated", result);
    assertTrue(user.getStatus()); // Đảm bảo trạng thái người dùng đã được cập nhật thành true

    // Xác nhận repository đã lưu lại người dùng
    verify(userRepository).save(user);
  }

  @Test
  void blockUnblockUser_userActivated_success() {
    // Arrange: Mô phỏng người dùng đã được kích hoạt (status = true)
    User user = new User();
    user.setId(1L);
    user.setStatus(true); // Người dùng đã được kích hoạt

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act: Thực hiện chặn người dùng
    String result = userService.blockUnblockUser(1L);

    // Assert: Kiểm tra kết quả trả về đúng
    assertNotNull(result);
    assertEquals("User has been blocked", result);
    assertFalse(user.getStatus()); // Đảm bảo trạng thái người dùng đã được cập nhật thành false

    // Xác nhận repository đã lưu lại người dùng
    verify(userRepository).save(user);
  }

  @Test
  void blockUnblockUser_userNotFound_fail() {
    // Arrange
    when(userRepository.findById(2L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AppException.class, () -> userService.blockUnblockUser(2L));
  }
}
