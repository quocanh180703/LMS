package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.dtos.User.UserDTO;
import com.example.nhom3_tt_.dtos.requests.LoginRequest;
import com.example.nhom3_tt_.dtos.requests.RegisterRequest;
import com.example.nhom3_tt_.models.ETypeRole;
import com.example.nhom3_tt_.security.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;

  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;
  private UserDTO userDTO;
  private ObjectMapper objectMapper;

  @BeforeEach
  void initData() {
    // init data
    objectMapper = new ObjectMapper();
    loginRequest = LoginRequest.builder().username("huy_cr311").password("123456789").build();
    registerRequest =
        RegisterRequest.builder()
            .username("huy_cr311")
            .password("123456789")
            .email("amazingshadow.dev@gmail.com")
            .fullName("Hoang Van Huy")
            .role(ETypeRole.STUDENT.name())
            .build();
    userDTO =
        UserDTO.builder()
            .username("huy_cr311")
            .email("amazingshadow.dev@gmail.com")
            .fullname("Hoang Van Huy")
            .phone("0915137869")
            .role(ETypeRole.STUDENT.name())
            .build();
  }

  @Test
  void register_validRequest_success() throws Exception {
    // GIVEN
    String content = objectMapper.writeValueAsString(registerRequest);
    when(authService.register(any())).thenReturn(userDTO);

    // WHEN AND THEN
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content)) // serialize object to json
        .andExpect(status().isCreated())
        .andExpect(jsonPath("statusCode").value(201))
        .andExpect(jsonPath("data.username").value(userDTO.getUsername()));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
          "u; validpass; valid@email.com; Valid Name; STUDENT; Username is at least 6 character and highest is 50 character",
          "user@name; validpass; valid@email.com; Valid Name; STUDENT; Username cannot contain special characters, emojis or space except _",
          "user name; validpass; valid@email.com; Valid Name; STUDENT; Username cannot contain space",
          "validuser; pa; valid@email.com; Valid Name; STUDENT; Password is at least 6 character and highest is 30 character",
          "validuser; pass word; valid@email.com; Valid Name; STUDENT; Password cannot contain space, emojis or special characters other than '@' and '.'",
          "validuser; validpass; invalid-email; Valid Name; STUDENT; Email is invalid, the email address must follow the format: user123@gmail.com",
          "validuser; validpass; valid@email.com; VN; STUDENT; Full name is at least 6 character and highest is 200 character",
          "validuser; validpass; valid@email.com; full@name; STUDENT; Full name cannot contain special characters or emojis except _",
              "validuser; validpass; valid@email.com; Valid Name; ; Role is required",
              "validuser; validpass; valid@email.com; Valid Name; INVALID_ROLE; Role must be either STUDENT or INSTRUCTOR",
          "validuser; validpass; valid@email.com; Valid Name; USER ROLE; Role must be either STUDENT or INSTRUCTOR"
      })
  void register_usernameInvalid_fail(
      String username,
      String password,
      String email,
      String fullName,
      String role,
      String expectedError)
      throws Exception {
    RegisterRequest invalidRequest = new RegisterRequest(username, password, email, fullName, role);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value(expectedError));
  }

  @Test
  void login_validRequest_success() throws Exception {
    // GIVEN
    ObjectNode response = objectMapper.createObjectNode();
    response.put(
        "access_token",
        "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjpbeyJhdXRob3JpdHkiOiJJTlNUUlVDVE9SIn1dLCJzdWIiOiJ0aGFuaG50NjJfMTIzIiwiaWF0IjoxNzM1NjE5NTY3LCJleHAiOjE3MzU2NTU1Njd9.i2RTJM3twewTSbTFOkXeyAHKVq6R2H0UD5VAUjBz66I0vJzlJGrGvv9dT6QPVxX41FrXEQzB_WKVSY-uAvPojw");
    String content = objectMapper.writeValueAsString(loginRequest);
    String jsonResponse = objectMapper.writeValueAsString(response);

    when(authService.login(any())).thenReturn(response);

    // WHEN AND THEN
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content)) // serialize object to json
        .andExpect(status().isOk())
        .andExpect(jsonPath("statusCode").value(200))
        .andExpect(
            jsonPath("data.access_token")
                .value(
                    "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjpbeyJhdXRob3JpdHkiOiJJTlNUUlVDVE9SIn1dLCJzdWIiOiJ0aGFuaG50NjJfMTIzIiwiaWF0IjoxNzM1NjE5NTY3LCJleHAiOjE3MzU2NTU1Njd9.i2RTJM3twewTSbTFOkXeyAHKVq6R2H0UD5VAUjBz66I0vJzlJGrGvv9dT6QPVxX41FrXEQzB_WKVSY-uAvPojw"));
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "username_invalid; password_valid; Invalid username or password",
        "username_valid; password_invalid; Invalid username or password",
      })
  void login_invalidRequest_fail(String username, String password, String expectedError)
      throws Exception {
    // GIVEN
    loginRequest.setUsername(username);
    loginRequest.setPassword(password);
    String content = objectMapper.writeValueAsString(loginRequest);
    when(authService.login(any())).thenThrow(new IllegalArgumentException(expectedError));
    // WHEN AND THEN
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(content))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("message").value(expectedError));
  }
}
