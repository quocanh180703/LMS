package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.nhom3_tt_.dtos.requests.ReviewRequest;
import com.example.nhom3_tt_.dtos.response.ReviewResponse;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.mappers.ReviewMapper;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Review;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.ReviewRepository;
import com.example.nhom3_tt_.services.impl.ReviewServiceImp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private CourseRepository courseRepository;
  @Mock
  private ReviewMapper reviewMapper;
  @Mock
  private Authentication authentication;
  @Mock
  private SecurityContext securityContext;
  @InjectMocks
  private ReviewServiceImp reviewService;

  @Test
  void getAll_Reviews_ByCourseId_success() {
    Pageable pageable = Pageable.unpaged();
    Long courseId = 1L;
    Review review = new Review();
    ReviewResponse reviewResponse = new ReviewResponse();

    when(reviewRepository.findAllReviewsByCourseId(courseId, pageable)).thenReturn(List.of(review));
    when(reviewMapper.convertToReviewResponse(review)).thenReturn(reviewResponse);

    List<ReviewResponse> result = reviewService.findAllReviewsByCourseId(courseId, pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(reviewResponse, result.get(0));

    verify(reviewRepository).findAllReviewsByCourseId(courseId, pageable);
    verify(reviewMapper).convertToReviewResponse(review);
  }

  @Test
  void create_Review_ToCourse_success() {
    Long courseId = 1L;
    User user = new User();
    user.setId(1L);

    Course course = new Course();
    course.setId(courseId);

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setStar(5.0);
    reviewRequest.setComment("Great course!");

    Review savedReview = Review.builder()
        .id(1L)
        .course(course)
        .student(user)
        .star(reviewRequest.getStar())
        .comment(reviewRequest.getComment())
        .build();

    ReviewResponse reviewResponse = new ReviewResponse();
    reviewResponse.setId(savedReview.getId());

    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(reviewRepository.findReviewByStudentIdAndCourseId(user.getId(), courseId)).thenReturn(
        null);
    when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
    when(reviewMapper.convertToReviewResponse(any(Review.class))).thenReturn(reviewResponse);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);

    ReviewResponse result = reviewService.addReviewToCourse(courseId, reviewRequest);

    assertNotNull(result);
    assertEquals(reviewResponse, result);

    verify(courseRepository).findById(courseId);
    verify(reviewRepository).findReviewByStudentIdAndCourseId(user.getId(), courseId);
    verify(reviewRepository).save(any(Review.class));
    verify(reviewMapper).convertToReviewResponse(any(Review.class));
  }

  @Test
  void addReviewToCourse_throwsExceptionWhenReviewAlreadyExists() {
    Long courseId = 1L;
    ReviewRequest reviewRequest = new ReviewRequest();

    Course mockCourse = new Course();
    mockCourse.setId(courseId);

    User mockUser = new User();
    mockUser.setId(1L);

    Review existingReview = new Review();

    when(courseRepository.findById(eq(courseId))).thenReturn(Optional.of(mockCourse));
    when(reviewRepository.findReviewByStudentIdAndCourseId(eq(mockUser.getId()),
        eq(courseId))).thenReturn(existingReview);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.addReviewToCourse(courseId, reviewRequest));
    assertEquals("You can only add 1 review in 1 course!", exception.getMessage());
    verify(courseRepository, times(1)).findById(eq(courseId));
    verify(reviewRepository, times(1)).findReviewByStudentIdAndCourseId(eq(mockUser.getId()),
        eq(courseId));
    verifyNoInteractions(reviewMapper);
    verify(reviewRepository, times(0)).save(any(Review.class));
  }

  @Test
  void get_Review_ById_success() {
    Long reviewId = 1L;
    ReviewResponse reviewResponse = new ReviewResponse();
    Review review = Review.builder().id(reviewId).build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    when(reviewMapper.convertToReviewResponse(review)).thenReturn(reviewResponse);

    ReviewResponse result = reviewService.findReviewById(reviewId);

    assertNotNull(result);
    assertEquals(reviewResponse, result);
    verify(reviewRepository, times(1)).findById(reviewId);
  }

  @Test
  void findReviewById_throwsExceptionWhenNotFound() {
    Long reviewId = 1L;
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.findReviewById(reviewId));
    assertEquals("Review not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());
  }

  @Test
  void updateReviewById_reviewNotFound() {
    Long reviewId = 1L;
    Long courseId = 2L;

    when(reviewRepository.findById(reviewId))
        .thenReturn(Optional.empty()); // Simulate review not found

    // Simulate exception thrown in case the review is not found
    assertThrows(CustomException.class,
        () -> reviewService.updateReviewById(reviewId, new ReviewRequest()));
    verify(reviewRepository).findById(reviewId); // Ensure findById is called
  }

  @Test
  void updateReviewById_courseNotFound() {
    Long reviewId = 1L;
    Long courseId = 2L;
    User mockUser = new User();
    mockUser.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(courseId);

    Review existingReview = Review.builder()
        .id(reviewId)
        .course(mockCourse) // Set a course that will not be found
        .student(mockUser)
        .star(4.0)
        .comment("Good course")
        .build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
    when(courseRepository.findById(courseId)).thenReturn(
        Optional.empty()); // Simulate course not found

    // Simulate exception thrown in case the course is not found
    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.updateReviewById(reviewId, new ReviewRequest()));

    assertEquals("Course not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(reviewRepository).findById(reviewId); // Ensure findById is called
    verify(courseRepository).findById(courseId); // Ensure courseRepository is called
  }

  @Test
  void updateReviewById_success() {
    Long reviewId = 1L;
    Long courseId = 2L;
    User mockUser = new User();
    mockUser.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(courseId);

    Review existingReview = Review.builder()
        .id(reviewId)
        .course(mockCourse)
        .student(mockUser)
        .star(4.0)
        .comment("Good course")
        .build();

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setStar(5.0);
    reviewRequest.setComment("Great course!");

    Review updatedReview = Review.builder()
        .id(reviewId)
        .course(mockCourse)
        .student(mockUser)
        .star(reviewRequest.getStar())
        .comment(reviewRequest.getComment())
        .build();

    ReviewResponse reviewResponse = new ReviewResponse();
    reviewResponse.setId(reviewId);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
    when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
    when(reviewMapper.convertToReviewResponse(any(Review.class))).thenReturn(reviewResponse);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    ReviewResponse result = reviewService.updateReviewById(reviewId, reviewRequest);

    assertNotNull(result);
    assertEquals(reviewResponse, result);
    verify(reviewRepository).findById(reviewId);
    verify(courseRepository).findById(courseId);
    verify(reviewRepository).save(any(Review.class));
    verify(reviewMapper).convertToReviewResponse(any(Review.class));
  }

  @Test
  void deleteReviewById_reviewNotFound() {
    Long reviewId = 1L;

    // Simulate review not found
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.deleteReviewById(reviewId));

    assertEquals("Review not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(reviewRepository).findById(reviewId); // Ensure findById is called
    verifyNoInteractions(courseRepository); // courseRepository should not be called
  }

  @Test
  void deleteReviewById_courseNotFound() {
    Long reviewId = 1L;
    Long courseId = 2L;
    User mockUser = new User();
    mockUser.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(courseId);

    Review existingReview = Review.builder()
        .id(reviewId)
        .course(mockCourse)
        .student(mockUser)
        .star(5.0)
        .comment("Great course!")
        .build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
    when(courseRepository.findById(courseId)).thenReturn(
        Optional.empty()); // Simulate course not found

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.deleteReviewById(reviewId));

    assertEquals("Course not found", exception.getMessage());
    assertEquals(404, exception.getStatusCode());

    verify(reviewRepository).findById(reviewId); // Ensure findById is called
    verify(courseRepository).findById(courseId); // Ensure courseRepository is called
    verifyNoMoreInteractions(reviewRepository); // Delete should not be called
  }

  @Test
  void deleteReviewById_success() {
    Long reviewId = 1L;
    Long courseId = 2L;
    User mockUser = new User();
    mockUser.setId(1L);

    Course mockCourse = new Course();
    mockCourse.setId(courseId);

    Review existingReview = Review.builder()
        .id(reviewId)
        .course(mockCourse)
        .student(mockUser)
        .star(5.0)
        .comment("Great course!")
        .build();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUser);
    SecurityContextHolder.setContext(securityContext);

    reviewService.deleteReviewById(reviewId);

    verify(reviewRepository).findById(reviewId);
    verify(courseRepository).findById(courseId);
    verify(reviewRepository).delete(existingReview);
  }

  @Test
  void validateReviewOwnership_success() {
    Long userId = 1L;

    User mockUser = new User();
    mockUser.setId(userId);

    Review mockReview = Review.builder()
        .student(mockUser)
        .build();

    assertDoesNotThrow(() -> reviewService.validateReviewOwnership(mockReview, userId));
  }

  @Test
  void validateReviewOwnership_throwsExceptionWhenUserNotOwner() {
    Long userId = 1L;
    Long anotherUserId = 2L;

    User mockUser = new User();
    mockUser.setId(anotherUserId);

    Review mockReview = Review.builder()
        .student(mockUser)
        .build();

    CustomException exception = assertThrows(CustomException.class,
        () -> reviewService.validateReviewOwnership(mockReview, userId));
    assertEquals("You are not allowed to modify or delete a record that you do not own.",
        exception.getMessage());
    assertEquals(403, exception.getStatusCode());
  }
}