package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.controllers.QuizSubmissionController;
import com.example.nhom3_tt_.dtos.requests.quizSubmission.QuizSubmissionRequest;
import com.example.nhom3_tt_.dtos.response.quizSubmission.QuizSubmissionResponse;
import com.example.nhom3_tt_.services.QuizSubmissionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizSubmissionControllerTest {
  @Mock private QuizSubmissionService quizSubmissionService;

  @InjectMocks private QuizSubmissionController quizSubmissionController;

  @Autowired private MockMvc mockMvc;

  @Test
  void getQuizSubmissionById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
    quizSubmissionResponse.setId(1L);
    when(quizSubmissionService.getById(1L)).thenReturn(quizSubmissionResponse);

    mockMvc.perform(get("/api/v1/quiz-submissions/1")).andExpect(status().isOk());
  }

  @Test
  void createQuizSubmission_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
    when(quizSubmissionService.create(any(QuizSubmissionRequest.class)))
        .thenReturn(quizSubmissionResponse);

    mockMvc
        .perform(
            post("/api/v1/quiz-submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"quizId\":1,\"totalTimes\":30,\"answers\":[{\"questionId\":1,\"answer\":2}]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(quizSubmissionResponse.getId()));
  }

  @Test
  void getQuizSubmissionByStudentId_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
    when(quizSubmissionService.getByStudentId(1L)).thenReturn(List.of(quizSubmissionResponse));

    mockMvc
        .perform(get("/api/v1/quiz-submissions/student/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getQuizSubmissionByLoggedInStudent_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
    when(quizSubmissionService.getByLoggedInUser()).thenReturn(List.of(quizSubmissionResponse));

    mockMvc
        .perform(get("/api/v1/quiz-submissions/student/logged-in"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void getQuizSubmissionByQuizId_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
    when(quizSubmissionService.getByQuizId(1L)).thenReturn(List.of(quizSubmissionResponse));

    mockMvc
        .perform(get("/api/v1/quiz-submissions/quiz/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void deleteQuizSubmissionById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizSubmissionController).build();
    doNothing().when(quizSubmissionService).deleteById(1L);

    quizSubmissionService.deleteById(1L);

    mockMvc.perform(delete("/api/v1/quiz-submissions/1")).andExpect(status().isOk());
  }
}
