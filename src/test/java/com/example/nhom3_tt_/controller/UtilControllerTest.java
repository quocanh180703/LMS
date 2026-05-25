package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.dtos.requests.UserChangePasswordOTPRequest;
import com.example.nhom3_tt_.dtos.requests.UserConfirmOTPRequest;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.services.UserService;
import com.example.nhom3_tt_.util.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UtilControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private MailService mailService;

  @MockBean private UserService userService;

  private UserConfirmOTPRequest userConfirmOTPRequest;
  private UserChangePasswordOTPRequest userChangePasswordOTPRequest;
  private User user;

  @BeforeEach
  void initData() throws MessagingException, UnsupportedEncodingException {
    userConfirmOTPRequest = new UserConfirmOTPRequest();
    userConfirmOTPRequest.setOTP("123456");
    userConfirmOTPRequest.setEmail("test@example.com");

    userChangePasswordOTPRequest = new UserChangePasswordOTPRequest();
    userChangePasswordOTPRequest.setOtp("123456");
    userChangePasswordOTPRequest.setEmail("test@example.com");
    userChangePasswordOTPRequest.setNewPassword("newPassword");
    userChangePasswordOTPRequest.setConfirmPassword("newPassword");

    user = new User();
  }

  @Test
  void sendEmail_validRequest_success() throws Exception {
    //
    when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));

    when(mailService.sendEmail(any(), anyString()))
        .thenReturn(CompletableFuture.completedFuture("Email sent"));

    mockMvc
        .perform(post("/api/v1/util/forgot-password").param("email", "test@example.com"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("message").value("CALL API SUCCESS!"));
  }

  @Test
  void confirmOTP_validRequest_success() throws Exception {

    String content = objectMapper.writeValueAsString(userConfirmOTPRequest);

    when(userService.confirmOtp(any())).thenReturn(true);

    mockMvc
        .perform(
            post("/api/v1/util/confirm-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("data").value(true));
  }

  @Test
  void changePasswordByOTP_validRequest_success() throws Exception {
    String content = objectMapper.writeValueAsString(userChangePasswordOTPRequest);

    when(userService.changePasswordOTP(any())).thenReturn("Password changed");

    mockMvc
        .perform(
            put("/api/v1/util/change-password-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("data.message").value("Password changed"));
  }

  @Test
  void blockUnblockUser_validRequest_success() throws Exception {
    when(userService.blockUnblockUser(1L)).thenReturn("User blocked");

    mockMvc
        .perform(put("/api/v1/util/block-unblock/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("User blocked"));
  }
}
