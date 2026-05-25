package com.example.nhom3_tt_.service;

import static com.example.nhom3_tt_.exception.ErrorCode.USER_NAME_EXISTED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.User.UserDTO;
import com.example.nhom3_tt_.dtos.requests.LoginRequest;
import com.example.nhom3_tt_.dtos.requests.RegisterRequest;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.ErrorCode;
import com.example.nhom3_tt_.models.ETypeRole;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.security.AuthService;
import com.example.nhom3_tt_.security.jwt.JwtService;
import com.example.nhom3_tt_.services.UserService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserService userService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthService authService;

  private LoginRequest loginRequest;

  private RegisterRequest registerRequest;

  private User user;

  @BeforeEach
  void initData() {
    registerRequest =
        RegisterRequest.builder()
            .username("huy_cr311")
            .password("123456789")
            .fullName("Hoang Van Huy")
            .email("amazingshadow.dev@gmail.com")
            .role(ETypeRole.STUDENT.name())
            .build();

    loginRequest = LoginRequest.builder().username("huy_cr311").password("123456789").build();

    user =
        User.builder()
            .username("huy_cr311")
            .password("123456789")
            .fullname("Hoang Van Huy")
            .email("amazingshadow.dev@gmail.com")
            .role(ETypeRole.STUDENT)
            .build();
  }

  @Test
  void testRegisterSuccessful() {
    // GIVEN
    when(userService.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
    when(userService.save(any(User.class))).thenReturn(user);

    // WHEN
    UserDTO result = authService.register(registerRequest);

    // THEN
    assertNotNull(result);
    assertEquals(user.getUsername(), result.getUsername());
    assertEquals(user.getFullname(), result.getFullname());
    assertEquals(user.getEmail(), result.getEmail());
    assertEquals(ETypeRole.STUDENT.name(), result.getRole());

    verify(userService).findByUsername(registerRequest.getUsername());
    verify(passwordEncoder).encode(registerRequest.getPassword());
    verify(userService).save(any(User.class));
  }

  @Test
  void testRegisterUsernameExists() {
    // GIVEN
    when(userService.findByUsername(registerRequest.getUsername()))
        .thenReturn(Optional.of(new User()));

    // WHEN
    AppException exception =
        assertThrows(AppException.class, () -> authService.register(registerRequest));

    // THEN
    assertEquals(USER_NAME_EXISTED, exception.getErrorCode());

    verify(userService).findByUsername(registerRequest.getUsername());
    verify(userService, never()).save(any(User.class));
  }

  @Test
  void testRegisterEmailExists() {
    // GIVEN
    when(userService.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
    when(userService.save(any(User.class)))
        .thenThrow(new DataIntegrityViolationException("Default message"));

    // WHEN
    AppException exception =
        assertThrows(AppException.class, () -> authService.register(registerRequest));

    // THEN
    assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());

    verify(userService).findByUsername(registerRequest.getUsername());
    verify(passwordEncoder).encode(registerRequest.getPassword());
    verify(userService).save(any(User.class));
  }

  @Test
  void testLoginSuccessful() {
    // GIVEN
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userService.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
    when(jwtService.generateAccessToken(user)).thenReturn("jwt_token");

    // WHEN
    Object result = authService.login(loginRequest);

    // THEN
    // Assert
    assertInstanceOf(ObjectNode.class, result);
    ObjectNode jsonResult = (ObjectNode) result;
    assertEquals("jwt_token", jsonResult.get("access_token").asText());

    // Verify
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userService).findByUsername(loginRequest.getUsername());
    verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    verify(jwtService).generateAccessToken(user);
  }

  @Test
  void testLoginAuthenticationFailure() {
    // GIVEN
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new AuthenticationException("Authentication failed") {});

    // WHEN
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

    // THEN
    assertEquals("Invalid username or password", exception.getMessage());

    // Verify
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verifyNoInteractions(userService, passwordEncoder, jwtService);
  }

  @Test
  void testLoginUserNotFound() {
    // GIVEN
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userService.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

    // GIVEN
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

    // THEN
    assertEquals("Invalid username or password", exception.getMessage());

    // Verify
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userService).findByUsername(loginRequest.getUsername());
    verifyNoInteractions(passwordEncoder, jwtService);
  }

  @Test
  void testLoginPasswordMismatch() {
    // GIVEN
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userService.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

    // WHEN
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));

    // THEN
    assertEquals("Invalid username or password", exception.getMessage());

    // Verify
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userService).findByUsername(loginRequest.getUsername());
    verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    verifyNoInteractions(jwtService);
  }
}
