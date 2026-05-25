package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.controllers.QuizController;
import com.example.nhom3_tt_.dtos.requests.quiz.QuizRequest;
import com.example.nhom3_tt_.dtos.response.quiz.QuizResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuizResponseWithQuestion;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.ErrorCode;
import com.example.nhom3_tt_.exception.GlobalException;
import com.example.nhom3_tt_.services.QuizService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class QuizControllerTest {
  @Mock private QuizService quizService;

  @InjectMocks private QuizController quizController;

  private MockMvc mockMvc;

  @Test
  void getAllQuiz_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    QuizResponse quizResponse1 = new QuizResponse();
    QuizResponse quizResponse2 = new QuizResponse();
    when(quizService.getAll()).thenReturn(List.of(quizResponse1, quizResponse2));

    mockMvc
        .perform(get("/api/v1/quizzes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void getQuizById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    QuizResponse quizResponse = new QuizResponse();
    when(quizService.getQuizById(1L)).thenReturn(quizResponse);

    mockMvc
        .perform(get("/api/v1/quizzes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(quizResponse.getId()));
  }

  @Test
  void createQuiz_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    QuizResponse quizResponse = new QuizResponse();
    when(quizService.create(any(QuizRequest.class))).thenReturn(quizResponse);

    mockMvc
        .perform(
            post("/api/v1/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                            "{\"sectionId\":\"1\",\"title\":\"string\",\"description\":\"string\",\"timeLimit\":30,\"attemptAllowed\":1,\"startDate\":\"2027-01-02T07:23:31.148Z\",\"endDate\":\"2027-01-03T07:23:31.148Z\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void editQuiz_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    QuizRequest quizRequest = new QuizRequest();
    QuizResponse quizResponse = new QuizResponse();
    when(quizService.update(eq(1L), any(QuizRequest.class))).thenReturn(quizResponse);

    mockMvc
        .perform(
            patch("/api/v1/quizzes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Quiz\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(quizResponse.getId()));
  }

  @Test
  void deleteQuiz_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    doNothing().when(quizService).delete(1L);

    mockMvc.perform(delete("/api/v1/quizzes/1")).andExpect(status().isNoContent());
  }

  @Test
  void importExcelQuestion_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "sample content".getBytes()); // Nội dung không rỗng
    doNothing().when(quizService).importQuizUsingExcel(any(), eq(1L));

    mockMvc.perform(multipart("/api/v1/quizzes/1/questions").file(file)).andExpect(status().isOk());
  }

  @Test
  void importExcelQuestion_emptyFile() throws Exception {
    // Arrange
    mockMvc =
        MockMvcBuilders.standaloneSetup(quizController)
            .setControllerAdvice(new GlobalException()) // Assuming you have an exception handler
            .build();

    MockMultipartFile emptyFile =
        new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]);

    // Act & Assert
    mockMvc
        .perform(multipart("/api/v1/quizzes/1/questions").file(emptyFile))
        .andExpect(status().isBadRequest()) // Assuming your AppException returns 400 Bad Request
        .andExpect(
            result -> {
              assertTrue(result.getResolvedException() instanceof AppException);
              assertEquals(
                  ErrorCode.FILE_NOT_EMPTY,
                  ((AppException) result.getResolvedException()).getErrorCode());
            });
  }

  @Test
  void getQuestionsByQuizId_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(quizController).build();
    QuizResponseWithQuestion quizResponse = QuizResponseWithQuestion.builder().id(1L).build();
    when(quizService.getQuizQuestionById(1L)).thenReturn(quizResponse);

    mockMvc
        .perform(get("/api/v1/quizzes/1/questions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(quizResponse.getId()));
  }
}
