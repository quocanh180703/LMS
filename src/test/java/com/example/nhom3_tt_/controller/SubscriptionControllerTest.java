package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.dtos.requests.subscription.SubscriptionCreateRequest;
import com.example.nhom3_tt_.dtos.requests.subscription.SubscriptionDeleteRequest;
import com.example.nhom3_tt_.dtos.response.PageResponse;
import com.example.nhom3_tt_.dtos.response.instructor.InstructorDetailInfo;
import com.example.nhom3_tt_.dtos.response.instructor.StudentDetailInfo;
import com.example.nhom3_tt_.services.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private SubscriptionService subscriptionService;

  @Autowired private ObjectMapper objectMapper;

  private SubscriptionCreateRequest subscriptionCreateRequest;
  private SubscriptionDeleteRequest subscriptionDeleteRequest;
  private PageResponse<List<InstructorDetailInfo>> pageResponse;
  private PageResponse<List<StudentDetailInfo>> response;
  private InstructorDetailInfo instructorDetailInfo;

  @BeforeEach
  void initData() {
    subscriptionCreateRequest = new SubscriptionCreateRequest(1L);
    subscriptionDeleteRequest = new SubscriptionDeleteRequest(3L, 1L);
    // Khởi tạo dữ liệu mock
    // Create a mock PageResponse
    pageResponse =
        PageResponse.<List<InstructorDetailInfo>>builder()
            .pageNO(0)
            .pageSize(10)
            .totalPage(1)
            .items(
                List.of(
                    InstructorDetailInfo.builder()
                        .id(1L)
                        .fullName("John Doe")
                        .email("john@example.com")
                        .build(),
                    InstructorDetailInfo.builder()
                        .id(2L)
                        .fullName("Jane Smith")
                        .email("jane@example.com")
                        .build()))
            .build();

    response =
        PageResponse.<List<StudentDetailInfo>>builder()
            .pageNO(0)
            .pageSize(10)
            .totalPage(1)
            .items(
                List.of(
                    StudentDetailInfo.builder()
                        .id(1L)
                        .fullName("John Doe")
                        .email("john@example.com")
                        .build(),
                    StudentDetailInfo.builder()
                        .id(2L)
                        .fullName("Jane Smith")
                        .email("jane@example.com")
                        .build()))
            .build();
  }

  @Test
  @WithMockUser("testUser")
  void createSubscription_validRequest_success() throws Exception {

    String content = objectMapper.writeValueAsString(subscriptionCreateRequest);
    when(subscriptionService.createSubscription(any())).thenReturn(content);

    mockMvc
        .perform(
            post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON).content(content))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("statusCode").value(201));
  }

  @Test
  @WithMockUser("testUser")
  void unSubscribe_validRequest_success() throws Exception {

    String content = objectMapper.writeValueAsString(subscriptionCreateRequest);
    when(subscriptionService.createSubscription(any())).thenReturn(content);

    mockMvc
        .perform(
            delete("/api/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "testUser", authorities = "STUDENT")
  void getAllFollowing_validRequest_success() throws Exception {

    when(subscriptionService.getAllSubscriptions(0, 10)).thenReturn(pageResponse);

    mockMvc
        .perform(
            get("/api/v1/subscriptions/student")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageNO").value(0))
        .andExpect(jsonPath("$.data.pageSize").value(10))
        .andExpect(jsonPath("$.data.totalPage").value(1));

    verify(subscriptionService, times(1)).getAllSubscriptions(0, 10);
  }

  @Test
  @WithMockUser(username = "testUser", authorities = "INSTRUCTOR")
  void getAllFollowingByInstructor_validRequest_success() throws Exception {
    when(subscriptionService.getAllSubscriber(0, 10)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/subscriptions/instructor")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageNO").value(0))
        .andExpect(jsonPath("$.data.pageSize").value(10))
        .andExpect(jsonPath("$.data.totalPage").value(1));

    verify(subscriptionService, times(1)).getAllSubscriber(0, 10);
  }

  @Test
  void getAllFollowingByInstructor_guest_fail() throws Exception {
    when(subscriptionService.getAllSubscriber(0, 10)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/subscriptions/instructor")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "testUser", authorities = "STUDENT")
  void getAllFollowingByInstructor_accessDenied_fail() throws Exception {
    when(subscriptionService.getAllSubscriber(0, 10)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/v1/subscriptions/instructor")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("error").value("Access Denied"))
        .andExpect(jsonPath("message").value("You don't have permission to do this action"));
  }
}
