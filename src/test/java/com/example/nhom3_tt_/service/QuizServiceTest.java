package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.requests.quiz.QuestionRequest;
import com.example.nhom3_tt_.dtos.requests.quiz.QuizRequest;
import com.example.nhom3_tt_.dtos.response.quiz.QuizResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuizResponseWithQuestion;
import com.example.nhom3_tt_.mappers.QuizMapper;
import com.example.nhom3_tt_.models.Question;
import com.example.nhom3_tt_.models.QuestionOption;
import com.example.nhom3_tt_.models.Quiz;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.models.embeddedId.QuestionOptionId;
import com.example.nhom3_tt_.repositories.QuestionRepository;
import com.example.nhom3_tt_.repositories.QuizRepository;
import com.example.nhom3_tt_.repositories.SectionRepository;
import com.example.nhom3_tt_.services.QuestionService;
import com.example.nhom3_tt_.services.impl.QuizServiceImpl;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {
  @Mock private QuizRepository quizRepository;
  @Mock private QuizMapper quizMapper;
  @Mock private QuestionService questionService;
  @Mock private SectionRepository sectionRepository;
  @InjectMocks private QuizServiceImpl quizService;
  @Mock private QuestionRepository questionRepository;

  @Test
  void getQuizEntityById_success() {
    Quiz quiz = new Quiz();
    when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

    Quiz result = quizService.getQuizEntityById(1L);

    assertNotNull(result);
    assertEquals(quiz, result);
    verify(quizRepository).findById(1L);
  }

  @Test
  void getQuizById_success() {
    Quiz quiz = new Quiz();
    QuizResponse quizResponse = new QuizResponse();
    when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
    when(quizMapper.toQuizResponse(quiz)).thenReturn(quizResponse);

    QuizResponse result = quizService.getQuizById(1L);

    assertNotNull(result);
    assertEquals(quizResponse, result);
    verify(quizRepository).findById(1L);
    verify(quizMapper).toQuizResponse(quiz);
  }

  @Test
  void getQuizQuestionById_success() {
    Quiz quiz = new Quiz();
    quiz.setId(1L);
    quiz.setTitle("Test Quiz");
    quiz.setDescription("Description");
    quiz.setTimeLimit(60);
    quiz.setStartDate(LocalDateTime.now());
    quiz.setEndDate(LocalDateTime.now());
    quiz.setAttemptAllowed(3);
    Section section = new Section();
    section.setId(1L);
    quiz.setSection(section);
    QuestionOptionId questionOptionId = new QuestionOptionId();
    questionOptionId.setQuestionId(1L);
    questionOptionId.setId(1L);
    List<Question> questions =
        List.of(
            Question.builder()
                .quiz(quiz)
                .options(
                    List.of(
                        QuestionOption.builder()
                            .id(questionOptionId)
                            .question(Question.builder().id(1L).build())
                            .content("Option")
                            .build()))
                .build());
    quiz.setQuestions(questions);
    when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

    QuizResponseWithQuestion result = quizService.getQuizQuestionById(1L);

    assertNotNull(result);
    assertEquals(quiz.getId(), result.getId());
    assertEquals(quiz.getTitle(), result.getTitle());
    verify(quizRepository).findById(1L);
  }

  @Test
  void createQuiz_success() {
    QuizRequest quizRequest = new QuizRequest();
    quizRequest.setSectionId(1L);
    Quiz quiz = new Quiz();
    QuizResponse quizResponse = new QuizResponse();
    when(sectionRepository.findById(1L)).thenReturn(Optional.of(new Section()));
    when(quizMapper.toQuiz(quizRequest)).thenReturn(quiz);
    when(quizRepository.save(quiz)).thenReturn(quiz);
    when(quizMapper.toQuizResponse(quiz)).thenReturn(quizResponse);

    QuizResponse result = quizService.create(quizRequest);

    assertNotNull(result);
    assertEquals(quizResponse, result);
    verify(sectionRepository).findById(1L);
    verify(quizMapper).toQuiz(quizRequest);
    verify(quizRepository).save(quiz);
    verify(quizMapper).toQuizResponse(quiz);
  }

  @Test
  void createQuiz_sectionIdNotFound_throwsException() {
    QuizRequest quizRequest = new QuizRequest();
    quizRequest.setSectionId(1L);

    when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              quizService.create(quizRequest);
            });

    assertEquals("Section ID not found", exception.getMessage());
    verify(sectionRepository).findById(1L);
  }

  @Test
  void updateQuiz_success() {
    Long id = 1L;
    QuizRequest quizRequest = new QuizRequest();
    quizRequest.setTitle("Updated Title");
    quizRequest.setDescription("Updated Description");
    quizRequest.setTimeLimit(60);
    quizRequest.setAttemptAllowed(3);
    quizRequest.setStartDate(LocalDateTime.now());
    quizRequest.setEndDate(LocalDateTime.now());
    quizRequest.setSectionId(1L);
    Quiz quiz = new Quiz();
    quiz.setId(id);
    quiz.setSection(Section.builder().id(1L).build());
    when(quizRepository.findById(id)).thenReturn(Optional.of(quiz));
    when(quizRepository.save(quiz)).thenReturn(quiz);
    when(quizMapper.toQuizResponse(quiz)).thenReturn(new QuizResponse());

    QuizResponse result = quizService.update(id, quizRequest);

    assertNotNull(result);
    assertEquals("Updated Title", quiz.getTitle());
    verify(quizRepository).findById(id);
    verify(quizRepository).save(quiz);
    verify(quizMapper).toQuizResponse(quiz);
  }

  @Test
  void deleteQuiz_success() {
    Long id = 1L;
    List<Question> questionList = List.of(new Question());
    when(questionRepository.findByQuizId(id)).thenReturn(questionList);
    doNothing().when(questionRepository).deleteAll(questionList);
    doNothing().when(quizRepository).deleteById(id);

    quizService.delete(id);

    verify(questionRepository).findByQuizId(id);
    verify(questionRepository).deleteAll(questionList);
    verify(quizRepository).deleteById(id);
  }

  @Test
  void getAll_success() {
    Quiz quiz1 = new Quiz();
    Quiz quiz2 = new Quiz();
    QuizResponse quizResponse1 = new QuizResponse();
    QuizResponse quizResponse2 = new QuizResponse();

    when(quizRepository.findAll()).thenReturn(List.of(quiz1, quiz2));
    when(quizMapper.toQuizResponse(quiz1)).thenReturn(quizResponse1);
    when(quizMapper.toQuizResponse(quiz2)).thenReturn(quizResponse2);

    List<QuizResponse> result = quizService.getAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    //    assertEquals(quizResponse1, result.get(0));
    //    assertEquals(quizResponse2, result.get(1));
    verify(quizRepository).findAll();
    //    verify(quizMapper).toQuizResponse(quiz1);
    //    verify(quizMapper).toQuizResponse(quiz2);
  }

  @Test
  void importQuizUsingExcel_success() throws IOException {
    // Create a mock Excel file
    XSSFWorkbook workbook = new XSSFWorkbook();
    var sheet = workbook.createSheet("Sheet1");
    var headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Content");
    headerRow.createCell(1).setCellValue("Answer");
    headerRow.createCell(2).setCellValue("Option1");
    headerRow.createCell(3).setCellValue("Option2");

    var dataRow = sheet.createRow(1);
    dataRow.createCell(0).setCellValue("Question 1");
    dataRow.createCell(1).setCellValue(1);
    dataRow.createCell(2).setCellValue("Option 1");
    dataRow.createCell(3).setCellValue("Option 2");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            in);

    // Call the method
    quizService.importQuizUsingExcel(file, 1L);

    // Verify that the questionService.create method was called with the correct parameters
    verify(questionService, times(1)).create(any(QuestionRequest.class));
  }
}
