package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.EnrollController;
import com.example.nhom3_tt_.dtos.requests.EnrollRequest;
import com.example.nhom3_tt_.dtos.response.EnrollResponse;
import com.example.nhom3_tt_.mappers.EnrollMapper;
import com.example.nhom3_tt_.models.Enroll;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.EnrollRepository;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.EnrollService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class EnrollControllerTest {

  private MockMvc mockMvc;

  @Mock
  private EnrollRepository enrollRepository;

  @Mock
  private EnrollService enrollService;

  @Mock
  private EnrollMapper enrollMapper;

  @Mock
  private CourseRepository courseRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private EnrollController enrollController;

  @Test
  void getEnrollById_success() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(enrollController).build();

    EnrollResponse enrollResponse = new EnrollResponse();
    enrollResponse.setId(1L);
    enrollResponse.setStudentId(10L);
    enrollResponse.setCourseId(101L);

    when(enrollService.findById(1L)).thenReturn(enrollResponse);

    mockMvc.perform(get("/api/v1/enrolls/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.studentId").value(10L))
        .andExpect(jsonPath("$.courseId").value(101L));
  }

  @Test
  void deleteEnrollById_success() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(enrollController).build();

    doNothing().when(enrollService).deleteEnrollById(1L);

    mockMvc.perform(delete("/api/v1/enrolls/1"))
        .andExpect(status().isOk());

    verify(enrollService).deleteEnrollById(1L);
  }

  @Test
  void deleteEnroll_success() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(enrollController).build();

    doNothing().when(enrollService).deleteEnroll(1L, 1L);

    mockMvc.perform(delete("/api/v1/enrolls/student/1/course/1"))
        .andExpect(status().isOk());

    verify(enrollService).deleteEnroll(1L, 1L);
  }

  @Test
  void enroll_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(enrollController).build();

    EnrollRequest enrollRequest = new EnrollRequest();
    // Set properties of enrollRequest as needed
    enrollRequest.setStudentId(1L);
    enrollRequest.setCourseId(101L);

    Enroll enroll = new Enroll();
    enroll.setId(1L);
    // Set other properties of enroll as needed

    when(enrollService.enroll(any(EnrollRequest.class))).thenReturn(enroll);

    mockMvc.perform(post("/api/v1/enrolls/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(enrollRequest)))
        .andExpect(status().isCreated());
  }
}
