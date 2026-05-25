package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.controllers.QuestionController;
import com.example.nhom3_tt_.dtos.requests.quiz.QuestionRequest;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionResponseNoAnswer;
import com.example.nhom3_tt_.services.QuestionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class QuestionControllerTest {
  @Mock private QuestionService questionService;

  @InjectMocks private QuestionController questionController;

  @Autowired private MockMvc mockMvc;

  @Test
  void getAllQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponse questionResponse1 = new QuestionResponse();
    QuestionResponse questionResponse2 = new QuestionResponse();
    when(questionService.getAll()).thenReturn(List.of(questionResponse1, questionResponse2));

    mockMvc
        .perform(get("/api/v1/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void getQuestionById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponseNoAnswer questionResponse = QuestionResponseNoAnswer.builder().build();
    when(questionService.getQuestionByIdNoAnswer(1L)).thenReturn(questionResponse);

    mockMvc.perform(get("/api/v1/questions/1")).andExpect(status().isOk());
  }

  @Test
  void getQuestionByQuizId_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponse questionResponse = new QuestionResponse();
    when(questionService.getQuestionByQuizId(1L)).thenReturn(List.of(questionResponse));

    mockMvc
        .perform(get("/api/v1/questions/quiz/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void createQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponse questionResponse = new QuestionResponse();
    when(questionService.create(any(QuestionRequest.class))).thenReturn(questionResponse);

    mockMvc
        .perform(
            post("/api/v1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"content\":\"Question Content??\",\"options\":[\"option 1\",\"option 2\",\"option 3\",\"option 4\"],\"answer\":2,\"quizId\":1}"))
        .andExpect(status().isOk());
  }

  @Test
  void updateQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponse questionResponse = new QuestionResponse();
    when(questionService.update(eq(1L), any(QuestionRequest.class))).thenReturn(questionResponse);

    mockMvc
        .perform(
            put("/api/v1/questions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"content\":\"Question Content Updated??\",\"options\":[\"option 1\",\"option 2\",\"option 3\",\"option 4\"],\"answer\":2,\"quizId\":1}"))
        .andExpect(status().isOk());
  }

  @Test
  void patchQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    QuestionResponse questionResponse = new QuestionResponse();
    when(questionService.update(eq(1L), any(QuestionRequest.class))).thenReturn(questionResponse);

    mockMvc
        .perform(
            patch("/api/v1/questions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Patched Question\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    doNothing().when(questionService).delete(1L);

    mockMvc.perform(delete("/api/v1/questions/1")).andExpect(status().isNoContent());
  }
}
