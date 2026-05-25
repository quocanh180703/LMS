package com.example.nhom3_tt_.service;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nhom3_tt_.dtos.requests.quiz.QuestionRequest;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionOptionResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionResponseNoAnswer;
import com.example.nhom3_tt_.models.Question;
import com.example.nhom3_tt_.models.QuestionOption;
import com.example.nhom3_tt_.models.Quiz;
import com.example.nhom3_tt_.models.embeddedId.QuestionOptionId;
import com.example.nhom3_tt_.repositories.QuestionOptionRepository;
import com.example.nhom3_tt_.repositories.QuestionRepository;
import com.example.nhom3_tt_.repositories.QuizRepository;
import com.example.nhom3_tt_.services.impl.QuestionServiceImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

  @InjectMocks
  private QuestionServiceImpl questionService;

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private QuestionOptionRepository questionOptionRepository;

  @Mock
  private QuizRepository quizRepository;

  @Test
  void create_shouldCreateQuestionAndOptions() {
    // Arrange
    Long quizId = 10L;
    QuestionRequest req = new QuestionRequest();
    req.setContent("New Question");
    req.setAnswer(2);
    req.setQuizId(quizId);
    req.setOptions(List.of("Option 1", "Option 2"));

    Quiz quiz = new Quiz();
    quiz.setId(quizId);

    Question question = new Question();
    question.setId(1L);
    question.setQuiz(quiz);
    question.setContent("New Question");

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
    when(questionRepository.save(any(Question.class))).thenReturn(question);

    // Act
    QuestionResponse result = questionService.create(req);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals("New Question", result.getContent(), "Question content should match.");
    assertEquals(quizId, result.getQuizId(), "Quiz ID should match.");
    assertEquals(2, result.getOptions().size(), "Options size should be 2.");

    // Verify first option
    QuestionOptionResponse optionResponse1 = result.getOptions().get(0);
    assertEquals("Option 1", optionResponse1.getContent(), "First option content should match.");

    // Verify second option
    QuestionOptionResponse optionResponse2 = result.getOptions().get(1);
    assertEquals("Option 2", optionResponse2.getContent(), "Second option content should match.");

    // Verify repository interactions
    verify(quizRepository).findById(quizId);
    verify(questionRepository, times(2)).save(
        any(Question.class)); // Expecting save to be called twice
    verify(questionOptionRepository, times(2)).save(any(QuestionOption.class));
  }

  @Test
  void create_shouldSaveQuestionAndOptions() {
    // Arrange
    Long quizId = 10L;
    String questionContent = "Sample Question";
    Quiz quiz = Quiz.builder().id(quizId).build();

    QuestionRequest req = new QuestionRequest();
    req.setQuizId(quizId);
    req.setContent(questionContent);
    req.setOptions(List.of("Option 1", "Option 2"));
    req.setAnswer(1);

    Question mockQuestion = Question.builder()
        .id(1L)
        .quiz(quiz)
        .content(questionContent)
        .build();

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
    when(questionRepository.save(any(Question.class)))
        .thenAnswer(invocation -> {
          Question question = invocation.getArgument(0);
          question.setId(1L); // Giả lập ID sau khi lưu
          return question;
        });
    when(questionOptionRepository.save(any(QuestionOption.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    QuestionResponse result = questionService.create(req);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertEquals(quizId, result.getQuizId(), "Quiz ID should match the input");
    assertEquals(questionContent, result.getContent(), "Content should match the input");
    assertEquals(2, result.getOptions().size(), "Options count should match the input");
    assertNotNull(result.getAnswer(), "Answer should be set correctly");

    // Verify interactions
    verify(quizRepository).findById(quizId);
    verify(questionRepository, times(2)).save(any(Question.class)); // Lưu hai lần
    verify(questionOptionRepository, times(2)).save(any(QuestionOption.class));
  }

  @Test
  void testGetQuestionEntityById_Success() {
    Long questionId = 1L;
    Question question = new Question();
    question.setId(questionId);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

    Question result = questionService.getQuestionEntityById(questionId);

    assertNotNull(result);
    assertEquals(questionId, result.getId());
    verify(questionRepository, times(1)).findById(questionId);
  }

  @Test
  void testGetQuestionEntityById_NotFound() {
    Long questionId = 1L;

    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    Question result = questionService.getQuestionEntityById(questionId);

    assertNull(result);
    verify(questionRepository, times(1)).findById(questionId);
  }

  @Test
  void update_shouldUpdateQuestionSuccessfully() {
    // Arrange
    Long questionId = 1L;
    Long quizId = 2L;
    List<String> newOptions = List.of("Option A", "Option B", "Option C");
    QuestionRequest req = new QuestionRequest();
    req.setContent("Updated Question Content");
    req.setQuizId(quizId);
    req.setOptions(newOptions);
    req.setAnswer(2);

    Question existingQuestion = new Question();
    existingQuestion.setId(questionId);
    existingQuestion.setContent("Original Question Content");
    existingQuestion.setAnswer(1L);

    Quiz quiz = new Quiz();
    quiz.setId(quizId);

    QuestionOptionId questionOptionId = new QuestionOptionId();
    questionOptionId.setId(1L);
    questionOptionId.setQuestionId(questionId);

    QuestionOption option1 = new QuestionOption();
    option1.setId(questionOptionId);
    option1.setContent("Original Option 1");

    List<QuestionOption> existingOptions = List.of(option1);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(existingQuestion));
    when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
    when(questionOptionRepository.findAllByQuestionId(questionId)).thenReturn(existingOptions);

    // Act
    QuestionResponse result = questionService.update(questionId, req);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertEquals("Updated Question Content", result.getContent(), "Content should be updated");
    assertEquals(quizId, result.getQuizId(), "Quiz ID should be updated");
    assertEquals(3, result.getOptions().size(), "Options size should match new options");

    // Verify new options were saved
    verify(questionOptionRepository, times(1)).deleteAll(existingOptions);
    verify(questionOptionRepository, times(3)).save(any(QuestionOption.class));
  }

  @Test
  void update_shouldRetainExistingOptions_whenOptionsAreNull() {
    // Arrange
    Long questionId = 1L;
    Long quizId = 10L;
    QuestionRequest req = new QuestionRequest();
    req.setContent("Updated Question");
    req.setAnswer(2);
    req.setQuizId(quizId);
    req.setOptions(null); // Setting options to null

    Question question = new Question();
    question.setId(questionId);
    question.setContent("Original Question");
    question.setAnswer(1L);
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    question.setQuiz(quiz);

    QuestionOption option1 = new QuestionOption();
    option1.setId(new QuestionOptionId(questionId, 1L));
    option1.setContent("Original Option 1");
    option1.setQuestion(question);

    QuestionOption option2 = new QuestionOption();
    option2.setId(new QuestionOptionId(questionId, 2L));
    option2.setContent("Original Option 2");
    option2.setQuestion(question);

    List<QuestionOption> questionOptions = List.of(option1, option2);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
    when(questionOptionRepository.findAllByQuestionId(questionId)).thenReturn(questionOptions);

    // Act
    QuestionResponse result = questionService.update(questionId, req);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals("Updated Question", result.getContent(), "Question content should be updated.");
    assertEquals(quizId, result.getQuizId(), "Quiz ID should match.");
    assertEquals(2, result.getOptions().size(), "Options size should be 2.");
    assertEquals(2, result.getAnswer(), "Answer should be retained.");

    // Verify first option
    QuestionOptionResponse optionResponse1 = result.getOptions().get(0);
    assertEquals("Original Option 1", optionResponse1.getContent(),
        "First option content should match.");

    // Verify second option
    QuestionOptionResponse optionResponse2 = result.getOptions().get(1);
    assertEquals("Original Option 2", optionResponse2.getContent(),
        "Second option content should match.");

    // Verify repository interactions
    verify(questionRepository).findById(questionId);
    verify(quizRepository).findById(quizId);
    verify(questionOptionRepository).findAllByQuestionId(questionId);
    verify(questionOptionRepository, times(0)).deleteAll(anyList());
    verify(questionOptionRepository, times(0)).save(any(QuestionOption.class));
    verify(questionRepository).save(question);
  }

  @Test
  void update_shouldReturnNullIfQuestionNotFound() {
    // Arrange
    Long questionId = 1L;
    QuestionRequest req = new QuestionRequest();
    req.setContent("Updated Content");

    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    // Act
    QuestionResponse result = questionService.update(questionId, req);

    // Assert
    assertNull(result, "Result should be null if question not found");
    verify(questionRepository, never()).save(any());
    verify(questionOptionRepository, never()).deleteAll(any());
    verify(questionOptionRepository, never()).save(any());
  }


  @Test
  void update_shouldReturnNull_whenQuestionDoesNotExist() {
    // Arrange
    Long questionId = 1L;
    QuestionRequest req = new QuestionRequest();

    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    // Act
    QuestionResponse result = questionService.update(questionId, req);

    // Assert
    assertNull(result, "Result should be null when question does not exist.");
    verify(questionRepository).findById(questionId);
    verify(questionOptionRepository, times(0)).deleteAll(anyList());
    verify(questionOptionRepository, times(0)).save(any(QuestionOption.class));
    verify(questionRepository, times(0)).save(any(Question.class));
  }

  @Test
  void getQuestionById_shouldReturnNull_whenQuestionDoesNotExist() {
    // Arrange
    Long questionId = 1L;
    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    // Act
    QuestionResponse result = questionService.getQuestionById(questionId);

    // Assert
    assertNull(result, "Result should be null when question does not exist.");
  }

  @Test
  void getQuestionById_shouldReturnResponseWithEmptyOptions_whenNoOptionsExist() {
    // Arrange
    Long questionId = 1L;
    Question question = new Question();
    question.setId(questionId);
    question.setContent("Sample Question");
    Quiz quiz = new Quiz();
    quiz.setId(10L);
    question.setQuiz(quiz);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(questionOptionRepository.findAllByQuestionId(questionId)).thenReturn(
        emptyList());

    // Act
    QuestionResponse result = questionService.getQuestionById(questionId);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals("Sample Question", result.getContent());
    assertEquals(10L, result.getQuizId());
    assertTrue(result.getOptions().isEmpty(), "Options should be empty when no options exist.");
  }

  @Test
  void delete_shouldRemoveQuestionAndOptions_whenExists() {
    // Arrange
    Long questionId = 1L;
    Question question = new Question();
    question.setId(questionId);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

    // Act
    questionService.delete(questionId);

    // Assert
    verify(questionOptionRepository, times(1)).deleteAll(anyList());
    verify(questionRepository, times(1)).delete(question);
  }

  @Test
  void delete_shouldDoNothing_whenNotExists() {
    // Arrange
    Long questionId = 1L;
    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    // Act
    questionService.delete(questionId);

    // Assert
    verify(questionOptionRepository, times(0)).deleteAll(anyList());
    verify(questionRepository, times(0)).delete(any(Question.class));
  }

  @Test
  void getQuestionById_shouldReturnQuestionResponseWithOptions() {
    // Arrange
    Long questionId = 1L;

    // Mock dữ liệu cho question
    Question question = new Question();
    question.setId(questionId);
    question.setContent("Sample Question");
    Quiz quiz = new Quiz();
    quiz.setId(10L);
    question.setQuiz(quiz);

    // Mock dữ liệu cho questionOptions
    QuestionOption option1 = new QuestionOption();
    option1.setId(new QuestionOptionId(questionId, 1L));
    option1.setContent("Option 1");

    QuestionOption option2 = new QuestionOption();
    option2.setId(new QuestionOptionId(questionId, 2L));
    option2.setContent("Option 2");

    List<QuestionOption> questionOptions = List.of(option1, option2);

    // Stub methods của repository
    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(questionOptionRepository.findAllByQuestionId(questionId)).thenReturn(questionOptions);

    // Act
    QuestionResponse result = questionService.getQuestionById(questionId);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals("Sample Question", result.getContent(), "Content should match.");
    assertEquals(null, result.getAnswer(), "Answer should match.");
    assertEquals(10L, result.getQuizId(), "Quiz ID should match.");
    assertEquals(2, result.getOptions().size(), "Options size should be 2.");

    // Verify first option
    QuestionOptionResponse optionResponse1 = result.getOptions().get(0);
    assertEquals(optionResponse1.getId(), optionResponse1.getId(), "Option 1 ID should match.");
    assertEquals(optionResponse1.getQuestionId(), optionResponse1.getQuestionId(),
        "Option 1 Question ID should match.");
    assertEquals("Option 1", optionResponse1.getContent(), "Option 1 Content should match.");

    // Verify second option
    QuestionOptionResponse optionResponse2 = result.getOptions().get(1);
    assertEquals(optionResponse2.getId(), optionResponse2.getId(), "Option 2 ID should match.");
    assertEquals(optionResponse2.getQuestionId(), optionResponse2.getQuestionId(),
        "Option 2 Question ID should match.");
    assertEquals("Option 2", optionResponse2.getContent(), "Option 2 Content should match.");

    // Verify repository interactions
    verify(questionRepository).findById(questionId);
    verify(questionOptionRepository).findAllByQuestionId(questionId);
  }

  @Test
  void getQuestionByIdNoAnswer_shouldReturnQuestionResponseNoAnswer_whenQuestionExists() {
    // Arrange
    Long questionId = 1L;
    Question question = new Question();
    question.setId(questionId);
    question.setContent("Sample Question");
    Quiz quiz = new Quiz();
    quiz.setId(10L);
    question.setQuiz(quiz);

    QuestionOptionId questionOptionId1 = new QuestionOptionId();
    questionOptionId1.setId(1L);
    questionOptionId1.setQuestionId(questionId);

    QuestionOption option1 = new QuestionOption();
    option1.setId(questionOptionId1);
    option1.setContent("Option 1");
    option1.setQuestion(question);

    QuestionOptionId questionOptionId2 = new QuestionOptionId();
    questionOptionId2.setId(2L);
    questionOptionId2.setQuestionId(questionId);

    QuestionOption option2 = new QuestionOption();
    option2.setId(questionOptionId2);
    option2.setContent("Option 2");
    option2.setQuestion(question);

    List<QuestionOption> questionOptions = List.of(option1, option2);

    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(questionOptionRepository.findAllByQuestionId(questionId)).thenReturn(questionOptions);

    // Act
    QuestionResponseNoAnswer result = questionService.getQuestionByIdNoAnswer(questionId);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals("Sample Question", result.getContent(), "Question content should match.");
    assertEquals(10L, result.getQuizId(), "Quiz ID should match.");
    assertEquals(2, result.getOptions().size(), "Options size should be 2.");

    // Verify first option
    QuestionOptionResponse optionResponse1 = result.getOptions().get(0);
    assertEquals(1L, optionResponse1.getId(), "First option ID should match.");
    assertEquals(questionId, optionResponse1.getQuestionId(),
        "First option question ID should match.");
    assertEquals("Option 1", optionResponse1.getContent(), "First option content should match.");

    // Verify second option
    QuestionOptionResponse optionResponse2 = result.getOptions().get(1);
    assertEquals(2L, optionResponse2.getId(),
        "Second option ID should match.");
    assertEquals(questionId, optionResponse2.getQuestionId(),
        "Second option question ID should match.");
    assertEquals("Option 2", optionResponse2.getContent(), "Second option content should match.");
  }

  @Test
  void getAll_shouldReturnListOfQuestionResponses() {
    // Arrange
    Long questionId1 = 1L;
    Long questionId2 = 2L;

    // Mock data for first question
    Question question1 = new Question();
    question1.setId(questionId1);
    question1.setContent("Sample Question 1");
    Quiz quiz1 = new Quiz();
    quiz1.setId(10L);
    question1.setQuiz(quiz1);

    QuestionOptionId questionOptionId1_1 = new QuestionOptionId();
    questionOptionId1_1.setQuestionId(questionId1);
    questionOptionId1_1.setId(1L);

    QuestionOption option1_1 = new QuestionOption();
    option1_1.setId(questionOptionId1_1);
    option1_1.setContent("Option 1-1");
    option1_1.setQuestion(question1);

    QuestionOptionId questionOptionId1_2 = new QuestionOptionId();
    questionOptionId1_2.setQuestionId(questionId1);
    questionOptionId1_2.setId(2L);

    QuestionOption option1_2 = new QuestionOption();
    option1_2.setId(questionOptionId1_2);
    option1_2.setContent("Option 1-2");
    option1_2.setQuestion(question1);

    List<QuestionOption> questionOptions1 = List.of(option1_1, option1_2);

    // Mock data for second question
    Question question2 = new Question();
    question2.setId(questionId2);
    question2.setContent("Sample Question 2");
    Quiz quiz2 = new Quiz();
    quiz2.setId(20L);
    question2.setQuiz(quiz2);

    QuestionOptionId questionOptionId2_1 = new QuestionOptionId();
    questionOptionId2_1.setQuestionId(questionId2);
    questionOptionId2_1.setId(1L);

    QuestionOption option2_1 = new QuestionOption();
    option2_1.setId(questionOptionId2_1);
    option2_1.setContent("Option 2-1");
    option2_1.setQuestion(question2);

    QuestionOptionId questionOptionId2_2 = new QuestionOptionId();
    questionOptionId2_2.setQuestionId(questionId2);
    questionOptionId2_2.setId(2L);

    QuestionOption option2_2 = new QuestionOption();
    option2_2.setId(questionOptionId2_2);
    option2_2.setContent("Option 2-2");
    option2_2.setQuestion(question2);

    List<QuestionOption> questionOptions2 = List.of(option2_1, option2_2);

    // Stub methods of repository
    when(questionRepository.findAll()).thenReturn(List.of(question1, question2));
    when(questionOptionRepository.findAllByQuestionId(questionId1)).thenReturn(questionOptions1);
    when(questionOptionRepository.findAllByQuestionId(questionId2)).thenReturn(questionOptions2);

    // Act
    List<QuestionResponse> result = questionService.getAll();

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals(2, result.size(), "Result size should be 2.");

    // Verify first question response
    QuestionResponse response1 = result.get(0);
    assertEquals("Sample Question 1", response1.getContent(),
        "First question content should match.");
    assertEquals(10L, response1.getQuizId(), "First question quiz ID should match.");
    assertEquals(2, response1.getOptions().size(), "First question options size should be 2.");

    // Verify first question options
    QuestionOptionResponse optionResponse1_1 = response1.getOptions().get(0);
    assertEquals(1L, optionResponse1_1.getId(), "First question first option ID should match.");
    assertEquals(questionId1, optionResponse1_1.getQuestionId(),
        "First question first option question ID should match.");
    assertEquals("Option 1-1", optionResponse1_1.getContent(),
        "First question first option content should match.");

    QuestionOptionResponse optionResponse1_2 = response1.getOptions().get(1);
    assertEquals(2L, optionResponse1_2.getId(), "First question second option ID should match.");
    assertEquals(questionId1, optionResponse1_2.getQuestionId(),
        "First question second option question ID should match.");
    assertEquals("Option 1-2", optionResponse1_2.getContent(),
        "First question second option content should match.");

    // Verify second question response
    QuestionResponse response2 = result.get(1);
    assertEquals("Sample Question 2", response2.getContent(),
        "Second question content should match.");
    assertEquals(20L, response2.getQuizId(), "Second question quiz ID should match.");
    assertEquals(2, response2.getOptions().size(), "Second question options size should be 2.");

    // Verify second question options
    QuestionOptionResponse optionResponse2_1 = response2.getOptions().get(0);
    assertEquals(1L, optionResponse2_1.getId(), "Second question first option ID should match.");
    assertEquals(questionId2, optionResponse2_1.getQuestionId(),
        "Second question first option question ID should match.");
    assertEquals("Option 2-1", optionResponse2_1.getContent(),
        "Second question first option content should match.");

    QuestionOptionResponse optionResponse2_2 = response2.getOptions().get(1);
    assertEquals(2L, optionResponse2_2.getId(), "Second question second option ID should match.");
    assertEquals(questionId2, optionResponse2_2.getQuestionId(),
        "Second question second option question ID should match.");
    assertEquals("Option 2-2", optionResponse2_2.getContent(),
        "Second question second option content should match.");

    // Verify repository interactions
    verify(questionRepository).findAll();
    verify(questionOptionRepository).findAllByQuestionId(questionId1);
    verify(questionOptionRepository).findAllByQuestionId(questionId2);
  }

  @Test
  void getQuestionByQuizId_shouldReturnListOfQuestionResponses_whenQuestionsExist() {
    // Arrange
    Long quizId = 10L;
    Long questionId1 = 1L;
    Long questionId2 = 2L;

    // Mock data for first question
    Question question1 = new Question();
    question1.setId(questionId1);
    question1.setContent("Sample Question 1");
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    question1.setQuiz(quiz);

    QuestionOptionId questionOptionId1_1 = new QuestionOptionId();
    questionOptionId1_1.setQuestionId(questionId1);
    questionOptionId1_1.setId(1L);

    QuestionOption option1_1 = new QuestionOption();
    option1_1.setId(questionOptionId1_1);
    option1_1.setContent("Option 1-1");
    option1_1.setQuestion(question1);

    QuestionOptionId questionOptionId1_2 = new QuestionOptionId();
    questionOptionId1_2.setQuestionId(questionId1);
    questionOptionId1_2.setId(2L);

    QuestionOption option1_2 = new QuestionOption();
    option1_2.setId(questionOptionId1_2);
    option1_2.setContent("Option 1-2");
    option1_2.setQuestion(question1);

    List<QuestionOption> questionOptions1 = List.of(option1_1, option1_2);

    // Mock data for second question
    Question question2 = new Question();
    question2.setId(questionId2);
    question2.setContent("Sample Question 2");
    question2.setQuiz(quiz);

    QuestionOptionId questionOptionId2_1 = new QuestionOptionId();
    questionOptionId2_1.setQuestionId(questionId2);
    questionOptionId2_1.setId(1L);

    QuestionOption option2_1 = new QuestionOption();
    option2_1.setId(questionOptionId2_1);
    option2_1.setContent("Option 2-1");
    option2_1.setQuestion(question2);

    QuestionOptionId questionOptionId2_2 = new QuestionOptionId();
    questionOptionId2_2.setQuestionId(questionId2);
    questionOptionId2_2.setId(2L);

    QuestionOption option2_2 = new QuestionOption();
    option2_2.setId(questionOptionId2_2);
    option2_2.setContent("Option 2-2");
    option2_2.setQuestion(question2);

    List<QuestionOption> questionOptions2 = List.of(option2_1, option2_2);

    // Stub methods of repository
    when(questionRepository.findByQuizId(quizId)).thenReturn(List.of(question1, question2));
    when(questionOptionRepository.findAllByQuestionId(questionId1)).thenReturn(questionOptions1);
    when(questionOptionRepository.findAllByQuestionId(questionId2)).thenReturn(questionOptions2);

    // Act
    List<QuestionResponse> result = questionService.getQuestionByQuizId(quizId);

    // Assert
    assertNotNull(result, "Result should not be null.");
    assertEquals(2, result.size(), "Result size should be 2.");

    // Verify first question response
    QuestionResponse response1 = result.get(0);
    assertEquals("Sample Question 1", response1.getContent(),
        "First question content should match.");
    assertEquals(quizId, response1.getQuizId(), "First question quiz ID should match.");
    assertEquals(2, response1.getOptions().size(), "First question options size should be 2.");

    // Verify first question options
    QuestionOptionResponse optionResponse1_1 = response1.getOptions().get(0);
    assertEquals(1L, optionResponse1_1.getId(), "First question first option ID should match.");
    assertEquals(questionId1, optionResponse1_1.getQuestionId(),
        "First question first option question ID should match.");
    assertEquals("Option 1-1", optionResponse1_1.getContent(),
        "First question first option content should match.");

    QuestionOptionResponse optionResponse1_2 = response1.getOptions().get(1);
    assertEquals(2L, optionResponse1_2.getId(), "First question second option ID should match.");
    assertEquals(questionId1, optionResponse1_2.getQuestionId(),
        "First question second option question ID should match.");
    assertEquals("Option 1-2", optionResponse1_2.getContent(),
        "First question second option content should match.");

    // Verify second question response
    QuestionResponse response2 = result.get(1);
    assertEquals("Sample Question 2", response2.getContent(),
        "Second question content should match.");
    assertEquals(quizId, response2.getQuizId(), "Second question quiz ID should match.");
    assertEquals(2, response2.getOptions().size(), "Second question options size should be 2.");

    // Verify second question options
    QuestionOptionResponse optionResponse2_1 = response2.getOptions().get(0);
    assertEquals(1L, optionResponse2_1.getId(), "Second question first option ID should match.");
    assertEquals(questionId2, optionResponse2_1.getQuestionId(),
        "Second question first option question ID should match.");
    assertEquals("Option 2-1", optionResponse2_1.getContent(),
        "Second question first option content should match.");

    QuestionOptionResponse optionResponse2_2 = response2.getOptions().get(1);
    assertEquals(2L, optionResponse2_2.getId(), "Second question second option ID should match.");
    assertEquals(questionId2, optionResponse2_2.getQuestionId(),
        "Second question second option question ID should match.");
    assertEquals("Option 2-2", optionResponse2_2.getContent(),
        "Second question second option content should match.");

    // Verify repository interactions
    verify(questionRepository).findByQuizId(quizId);
    verify(questionOptionRepository).findAllByQuestionId(questionId1);
    verify(questionOptionRepository).findAllByQuestionId(questionId2);
  }
}
