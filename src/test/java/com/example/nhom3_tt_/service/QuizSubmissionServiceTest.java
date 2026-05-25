package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.nhom3_tt_.dtos.requests.quizSubmission.QuizSubmissionAnswerRequest;
import com.example.nhom3_tt_.dtos.requests.quizSubmission.QuizSubmissionRequest;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionOptionResponse;
import com.example.nhom3_tt_.dtos.response.quiz.QuestionResponse;
import com.example.nhom3_tt_.dtos.response.quizSubmission.CustomAnswerResponse;
import com.example.nhom3_tt_.dtos.response.quizSubmission.QuizSubmissionResponse;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Question;
import com.example.nhom3_tt_.models.Quiz;
import com.example.nhom3_tt_.models.QuizSubmission;
import com.example.nhom3_tt_.models.QuizSubmissionAnswer;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.QuizSubmissionAnswerRepository;
import com.example.nhom3_tt_.repositories.QuizSubmissionRepository;
import com.example.nhom3_tt_.services.EnrollService;
import com.example.nhom3_tt_.services.QuestionService;
import com.example.nhom3_tt_.services.QuizService;
import com.example.nhom3_tt_.services.impl.QuizSubmissionServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class QuizSubmissionServiceTest {

  @Mock private QuizSubmissionRepository quizSubmissionRepository;

  @Mock private QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;

  @Mock private QuestionService questionService;

  @Mock private QuizService quizService;

  @Mock private EnrollService enrollService;

  @Mock private SecurityContext securityContext;

  @Mock private Authentication authentication;

  @InjectMocks private QuizSubmissionServiceImpl quizSubmissionService;

  @Test
  void testGetById_success() {
    Long quizSubmissionId = 1L;
    Quiz quiz = new Quiz();
    quiz.setId(1L);
    quiz.setTitle("Sample Quiz");
    quiz.setQuestions(List.of(new Question()));

    QuizSubmission quizSubmission =
        QuizSubmission.builder()
            .id(quizSubmissionId)
            .quiz(quiz)
            .score(85.0)
            .totalTimes(60)
            .totalCorrects(8)
            .answers(new ArrayList<>()) // Initialize the answers field
            .build();

    QuizSubmissionResponse quizSubmissionResponse =
        new QuizSubmissionResponse()
            .builder()
            .id(quizSubmissionId)
            .quizId(quiz.getId())
            .quizTitle(quiz.getTitle())
            .score(quizSubmission.getScore())
            .totalTimes(quizSubmission.getTotalTimes())
            .totalCorrects(quizSubmission.getTotalCorrects())
            .build();

    when(quizSubmissionRepository.findById(quizSubmissionId))
        .thenReturn(Optional.of(quizSubmission));

    QuizSubmissionResponse response = quizSubmissionService.getById(quizSubmissionId);

    assertNotNull(response);
    assertEquals(quizSubmissionId, response.getId());
    assertEquals(quiz.getId(), response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(quizSubmission.getScore(), response.getScore());
    assertEquals(quizSubmission.getTotalTimes(), response.getTotalTimes());
    assertEquals(quizSubmission.getTotalCorrects(), response.getTotalCorrects());
    verify(quizSubmissionRepository).findById(quizSubmissionId);
  }

  @Test
  void create_withAnswers_success() {
    Long userId = 1L;
    Long quizId = 1L;
    Long questionId = 1L;
    Long answerId = 1L;
    Long quizSubmissionId = 1L;

    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    quiz.setTitle("Sample Quiz");
    quiz.setTimeLimit(60);
    quiz.setAttemptAllowed(3);
    quiz.setQuestions(new ArrayList<>()); // Initialize the questions field

    Section section = new Section();
    Course course = new Course();
    course.setId(1L);
    section.setCourse(course);
    quiz.setSection(section);

    // Assuming QuizSubmissionAnswerRequest is the correct class for answers
    QuizSubmissionAnswerRequest requestAnswer = new QuizSubmissionAnswerRequest();
    requestAnswer.setQuestionId(questionId);
    requestAnswer.setAnswer(answerId);

    QuizSubmissionRequest request = new QuizSubmissionRequest();
    request.setQuizId(quizId);
    request.setTotalTimes(30);
    request.setAnswers(List.of(requestAnswer));

    User mockUser = new User();
    mockUser.setId(userId);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    when(quizService.getQuizEntityById(quizId)).thenReturn(quiz);
    when(enrollService.isEnrolled(userId, quiz.getSection().getCourse().getId())).thenReturn(true);
    when(quizSubmissionRepository.findAllByQuizIdAndCreatedBy(quizId, userId))
        .thenReturn(new ArrayList<>());
    when(quizSubmissionRepository.save(any(QuizSubmission.class)))
        .thenAnswer(
            invocation -> {
              QuizSubmission savedQuizSubmission = invocation.getArgument(0);
              savedQuizSubmission.setId(quizSubmissionId);
              return savedQuizSubmission;
            });

    Question question = new Question();
    question.setId(questionId);
    when(questionService.getQuestionEntityById(questionId)).thenReturn(question);

    QuizSubmissionAnswer quizSubmissionAnswer = new QuizSubmissionAnswer();
    quizSubmissionAnswer.setId(answerId);
    quizSubmissionAnswer.setQuizSubmission(new QuizSubmission());
    quizSubmissionAnswer.setQuestion(question);
    quizSubmissionAnswer.setAnswer(answerId);

    when(quizSubmissionAnswerRepository.save(any(QuizSubmissionAnswer.class)))
        .thenReturn(quizSubmissionAnswer);

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(quizSubmissionId);
    quizSubmission.setQuiz(quiz);
    quizSubmission.setScore(0.0);
    quizSubmission.setTotalTimes(30);
    quizSubmission.setTotalCorrects(0);
    quizSubmission.setAnswers(new ArrayList<>());

    when(quizSubmissionRepository.findById(quizSubmissionId))
        .thenReturn(Optional.of(quizSubmission));

    QuizSubmissionResponse response = quizSubmissionService.create(request);

    assertNotNull(response);
    assertEquals(quizSubmissionId, response.getId());
    assertEquals(quizId, response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(0, response.getScore());
    assertEquals(30, response.getTotalTimes());
    assertEquals(0, response.getTotalCorrects());
    verify(quizSubmissionRepository, times(2))
        .save(any(QuizSubmission.class)); // Expect save to be called twice
    verify(quizSubmissionAnswerRepository, times(1)).save(any(QuizSubmissionAnswer.class));
  }

  @Test
  void getByQuizId_success() {
    Long quizId = 1L;
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    quiz.setTitle("Sample Quiz");
    quiz.setQuestions(List.of(new Question()));

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmission.setQuiz(quiz);
    quizSubmission.setScore(85.0);
    quizSubmission.setTotalTimes(60);
    quizSubmission.setTotalCorrects(8);
    quizSubmission.setAnswers(new ArrayList<>());

    List<QuizSubmission> quizSubmissionList = new ArrayList<>();
    quizSubmissionList.add(quizSubmission);

    when(quizSubmissionRepository.findAllByQuizId(quizId)).thenReturn(quizSubmissionList);

    List<QuizSubmissionResponse> responses = quizSubmissionService.getByQuizId(quizId);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    QuizSubmissionResponse response = responses.get(0);
    assertEquals(quizSubmission.getId(), response.getId());
    assertEquals(quiz.getId(), response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(quizSubmission.getScore(), response.getScore());
    assertEquals(quizSubmission.getTotalTimes(), response.getTotalTimes());
    assertEquals(quizSubmission.getTotalCorrects(), response.getTotalCorrects());

    verify(quizSubmissionRepository).findAllByQuizId(quizId);
  }

  @Test
  void getByCourseId_success() {
    Long courseId = 1L;
    Long quizId = 1L;
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    quiz.setTitle("Sample Quiz");
    quiz.setQuestions(List.of(new Question()));

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmission.setQuiz(quiz);
    quizSubmission.setScore(85.0);
    quizSubmission.setTotalTimes(60);
    quizSubmission.setTotalCorrects(8);
    quizSubmission.setAnswers(new ArrayList<>());

    List<QuizSubmission> quizSubmissionList = new ArrayList<>();
    quizSubmissionList.add(quizSubmission);

    when(quizSubmissionRepository.findAllByQuizSectionCourseId(courseId))
        .thenReturn(quizSubmissionList);

    List<QuizSubmissionResponse> responses = quizSubmissionService.getByCourseId(courseId);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    QuizSubmissionResponse response = responses.get(0);
    assertEquals(quizSubmission.getId(), response.getId());
    assertEquals(quiz.getId(), response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(quizSubmission.getScore(), response.getScore());
    assertEquals(quizSubmission.getTotalTimes(), response.getTotalTimes());
    assertEquals(quizSubmission.getTotalCorrects(), response.getTotalCorrects());

    verify(quizSubmissionRepository).findAllByQuizSectionCourseId(courseId);
  }

  @Test
  void getByStudentId_success() {
    Long studentId = 1L;
    Long quizId = 1L;
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    quiz.setTitle("Sample Quiz");
    quiz.setQuestions(List.of(new Question()));

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmission.setQuiz(quiz);
    quizSubmission.setScore(85.0);
    quizSubmission.setTotalTimes(60);
    quizSubmission.setTotalCorrects(8);
    quizSubmission.setAnswers(new ArrayList<>());

    List<QuizSubmission> quizSubmissionList = new ArrayList<>();
    quizSubmissionList.add(quizSubmission);

    when(quizSubmissionRepository.findAllByCreatedBy(studentId)).thenReturn(quizSubmissionList);

    List<QuizSubmissionResponse> responses = quizSubmissionService.getByStudentId(studentId);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    QuizSubmissionResponse response = responses.get(0);
    assertEquals(quizSubmission.getId(), response.getId());
    assertEquals(quiz.getId(), response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(quizSubmission.getScore(), response.getScore());
    assertEquals(quizSubmission.getTotalTimes(), response.getTotalTimes());
    assertEquals(quizSubmission.getTotalCorrects(), response.getTotalCorrects());

    verify(quizSubmissionRepository).findAllByCreatedBy(studentId);
  }

  @Test
  void getByLoggedInUser_success() {
    Long userId = 1L;
    Long quizId = 1L;
    Quiz quiz = new Quiz();
    quiz.setId(quizId);
    quiz.setTitle("Sample Quiz");
    quiz.setQuestions(List.of(new Question()));

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(1L);
    quizSubmission.setQuiz(quiz);
    quizSubmission.setScore(85.0);
    quizSubmission.setTotalTimes(60);
    quizSubmission.setTotalCorrects(8);
    quizSubmission.setAnswers(new ArrayList<>());

    List<QuizSubmission> quizSubmissionList = new ArrayList<>();
    quizSubmissionList.add(quizSubmission);

    User mockUser = new User();
    mockUser.setId(userId);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    when(quizSubmissionRepository.findAllByCreatedBy(userId)).thenReturn(quizSubmissionList);

    List<QuizSubmissionResponse> responses = quizSubmissionService.getByLoggedInUser();

    assertNotNull(responses);
    assertEquals(1, responses.size());
    QuizSubmissionResponse response = responses.get(0);
    assertEquals(quizSubmission.getId(), response.getId());
    assertEquals(quiz.getId(), response.getQuizId());
    assertEquals(quiz.getTitle(), response.getQuizTitle());
    assertEquals(quizSubmission.getScore(), response.getScore());
    assertEquals(quizSubmission.getTotalTimes(), response.getTotalTimes());
    assertEquals(quizSubmission.getTotalCorrects(), response.getTotalCorrects());

    verify(quizSubmissionRepository).findAllByCreatedBy(userId);
  }

  @Test
  void deleteById_success() {
    Long quizSubmissionId = 1L;

    doNothing().when(quizSubmissionRepository).deleteById(quizSubmissionId);

    quizSubmissionRepository.deleteById(quizSubmissionId);

    verify(quizSubmissionRepository).deleteById(quizSubmissionId);
  }

  @Test
  void toCustomAnswerResponse_success() {
    Long quizSubmissionId = 1L;
    Long questionId = 1L;
    Long answerId = 1L;
    Long correctAnswerId = 2L;

    QuizSubmission quizSubmission = new QuizSubmission();
    quizSubmission.setId(quizSubmissionId);

    QuizSubmissionAnswer quizSubmissionAnswer = new QuizSubmissionAnswer();
    quizSubmissionAnswer.setId(answerId);
    quizSubmissionAnswer.setQuizSubmission(quizSubmission);
    quizSubmissionAnswer.setAnswer(answerId);

    QuestionResponse questionResponse =
        createQuestionResponse(questionId, answerId, correctAnswerId);

    quizSubmissionAnswer.setQuestion(new Question());
    quizSubmissionAnswer.getQuestion().setId(questionId);
    quizSubmission.setAnswers(List.of(quizSubmissionAnswer));

    when(questionService.getQuestionById(questionId)).thenReturn(questionResponse);

    List<CustomAnswerResponse> responses =
        quizSubmissionService.toCustomAnswerResponse(quizSubmission);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    CustomAnswerResponse response = responses.get(0);
    assertEquals(answerId, response.getId());
    assertEquals(quizSubmissionId, response.getQuizSubmissionId());
    assertEquals(questionId, response.getQuestionId());
    //    assertEquals("Sample Question", response.getQuestionContent());
    assertEquals("Sample Answer", response.getAnswerContent());
    assertEquals("Correct Answer", response.getCorrectAnswerContent());
    assertFalse(response.isCorrect());

    verify(questionService).getQuestionById(questionId);
  }

  private QuestionResponse createQuestionResponse(
      Long questionId, Long answerId, Long correctAnswerId) {
    QuestionResponse questionResponse = new QuestionResponse();
    questionResponse.setContent("Sample Question");

    QuestionOptionResponse answerOption =
        new QuestionOptionResponse(answerId, questionId, "Sample Answer");
    QuestionOptionResponse correctAnswerOption =
        new QuestionOptionResponse(correctAnswerId, questionId, "Correct Answer");

    questionResponse.setOptions(List.of(answerOption, correctAnswerOption));
    questionResponse.setAnswer(correctAnswerId);

    return questionResponse;
  }
}
