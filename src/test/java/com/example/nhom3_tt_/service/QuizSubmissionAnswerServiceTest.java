package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.requests.quizSubmission.QuizSubmissionAnswerRequest;
import com.example.nhom3_tt_.dtos.response.quizSubmission.QuizSubmissionAnswerResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.models.Question;
import com.example.nhom3_tt_.models.QuizSubmission;
import com.example.nhom3_tt_.models.QuizSubmissionAnswer;
import com.example.nhom3_tt_.repositories.QuestionRepository;
import com.example.nhom3_tt_.repositories.QuizSubmissionAnswerRepository;
import com.example.nhom3_tt_.repositories.QuizSubmissionRepository;
import com.example.nhom3_tt_.services.impl.QuizSubmissionAnswerServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
class QuizSubmissionAnswerServiceTest {

  @Mock private QuizSubmissionAnswerRepository repository;

  @Mock private QuizSubmissionRepository quizSubmissionRepository;

  @Mock private QuestionRepository questionRepository;

  @InjectMocks private QuizSubmissionAnswerServiceImpl service;

  @BeforeEach
  void setUp() {
    // Tạo dữ liệu mock cho QuizSubmission
    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);

    // Tạo dữ liệu mock cho Question
    Question question = new Question();
    question.setId(2L);
    question.setContent("What is the answer to life?");

    // Tạo dữ liệu mock cho QuizSubmissionAnswer
    QuizSubmissionAnswer savedAnswer =
        QuizSubmissionAnswer.builder()
            .id(3L)
            .quizSubmission(quizSubmission)
            .question(question)
            .answer(42L)
            .build();

    // Mock hành vi của quizSubmissionRepository
    lenient().when(quizSubmissionRepository.findById(1L)).thenReturn(Optional.of(quizSubmission));

    // Mock hành vi của questionRepository
    lenient().when(questionRepository.findById(2L)).thenReturn(Optional.of(question));

    // Mock hành vi của repository.save
    lenient().when(repository.save(any(QuizSubmissionAnswer.class))).thenReturn(savedAnswer);
  }

  @Test
  void testCreate_Success() {
    // Arrange
    QuizSubmissionAnswerRequest request =
        QuizSubmissionAnswerRequest.builder()
            .quizSubmissionId(1L)
            .questionId(2L)
            .answer(42L)
            .build();

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);

    Question question = new Question();
    question.setId(2L);
    question.setContent("What is the answer to life?");

    QuizSubmissionAnswer savedAnswer =
        QuizSubmissionAnswer.builder()
            .id(3L)
            .quizSubmission(quizSubmission)
            .question(question)
            .answer(42L)
            .build();

    when(quizSubmissionRepository.findById(1L)).thenReturn(Optional.of(quizSubmission));
    when(questionRepository.findById(2L)).thenReturn(Optional.of(question));
    when(repository.save(any(QuizSubmissionAnswer.class))).thenReturn(savedAnswer);

    // Act
    QuizSubmissionAnswerResponse response = service.create(request);

    // Assert
    assertNotNull(response);
    assertEquals(3L, response.getId());
    assertEquals(1L, response.getQuizSubmissionId());
    assertEquals(2L, response.getQuestionId());
    assertEquals(42L, response.getAnswer());
    verify(quizSubmissionRepository).findById(1L);
    verify(questionRepository).findById(2L);
    verify(repository).save(any(QuizSubmissionAnswer.class));
  }

  @Test
  void testCreate_QuizSubmissionNotFound() {
    // Arrange
    QuizSubmissionAnswerRequest request =
        QuizSubmissionAnswerRequest.builder()
            .quizSubmissionId(1L)
            .questionId(2L)
            .answer(42L)
            .build();

    when(quizSubmissionRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> service.create(request));
    assertEquals("QuizSubmission cannot found!", exception.getMessage());
    verify(quizSubmissionRepository).findById(1L);
    verifyNoInteractions(questionRepository);
    verifyNoInteractions(repository);
  }

  @Test
  void testCreate_QuestionNotFound() {
    // Arrange
    QuizSubmissionAnswerRequest request =
        QuizSubmissionAnswerRequest.builder()
            .quizSubmissionId(1L)
            .questionId(2L)
            .answer(42L)
            .build();

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);

    when(quizSubmissionRepository.findById(1L)).thenReturn(Optional.of(quizSubmission));
    when(questionRepository.findById(2L)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> service.create(request));
    assertEquals("Question cannot found!", exception.getMessage());
    verify(quizSubmissionRepository).findById(1L);
    verify(questionRepository).findById(2L);
    verifyNoInteractions(repository);
  }

  @Test
  void testGetById_Success() {
    // Arrange
    Long id = 1L;
    QuizSubmissionAnswer quizSubmissionAnswer = new QuizSubmissionAnswer();
    quizSubmissionAnswer.setId(id);
    quizSubmissionAnswer.setAnswer(42L);

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmissionAnswer.setQuizSubmission(quizSubmission);

    Question question = new Question();
    question.setId(2L);
    quizSubmissionAnswer.setQuestion(question);

    when(repository.findById(id)).thenReturn(Optional.of(quizSubmissionAnswer));

    // Act
    QuizSubmissionAnswerResponse response = service.getById(id);

    // Assert
    assertNotNull(response);
    assertEquals(id, response.getId());
    assertEquals(1L, response.getQuizSubmissionId());
    assertEquals(2L, response.getQuestionId());
    assertEquals(42L, response.getAnswer());
    verify(repository).findById(id);
  }

  @Test
  void testGetById_NotFound() {
    // Arrange
    Long id = 1L;
    when(repository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class, () -> service.getById(id));
    assertEquals("QuizSubmissionAnswer cannot found", exception.getMessage());
    verify(repository).findById(id);
  }

  @Test
  void testDeleteById_Success() {
    // Arrange
    Long id = 1L;
    when(repository.existsById(id)).thenReturn(true);

    // Act
    service.deleteById(id);

    // Assert
    verify(repository).existsById(id);
    verify(repository).deleteById(id);
  }

  @Test
  void testDeleteById_NotFound() {
    // Arrange
    Long id = 1L;
    when(repository.existsById(id)).thenReturn(false);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> service.deleteById(id));
    assertEquals("QuizSubmissionAnswer not found with id: " + id, exception.getMessage());

    // Kiểm tra rằng không có hành vi nào khác với repository sau khi không tìm thấy id
    verify(repository).existsById(id);
    verifyNoMoreInteractions(repository); // Xác nhận không có tương tác nào khác ngoài existsById
  }

  @Test
  void testUpdate_Success() {
    // Arrange
    Long id = 1L;
    QuizSubmissionAnswer quizSubmissionAnswer = new QuizSubmissionAnswer();
    quizSubmissionAnswer.setId(id);
    quizSubmissionAnswer.setAnswer(42L);

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmissionAnswer.setQuizSubmission(quizSubmission);

    Question question = new Question();
    question.setId(2L);
    quizSubmissionAnswer.setQuestion(question);

    QuizSubmissionAnswerRequest request = new QuizSubmissionAnswerRequest();
    request.setAnswer(100L);

    when(repository.findById(id)).thenReturn(Optional.of(quizSubmissionAnswer));
    when(repository.save(any(QuizSubmissionAnswer.class))).thenReturn(quizSubmissionAnswer);

    // Act
    QuizSubmissionAnswerResponse response = service.update(id, request);

    // Assert
    assertNotNull(response);
    assertEquals(id, response.getId());
    assertEquals(100L, response.getAnswer()); // Kiểm tra đã cập nhật answer
    verify(repository).findById(id);
    verify(repository).save(any(QuizSubmissionAnswer.class));
  }

  @Test
  void testUpdate_NotFound() {
    // Arrange
    Long id = 1L;
    QuizSubmissionAnswerRequest request = new QuizSubmissionAnswerRequest();
    request.setAnswer(100L);

    when(repository.findById(id)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> service.update(id, request));
    assertEquals("QuizSubmissionAnswer cannot found", exception.getMessage());

    // Kiểm tra rằng không có hành vi nào khác với repository sau khi không tìm thấy id
    verify(repository).findById(id);
    verifyNoMoreInteractions(repository); // Xác nhận không có tương tác nào khác ngoài findById
  }
}
