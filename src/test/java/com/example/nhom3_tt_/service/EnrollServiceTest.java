package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.requests.EnrollRequest;
import com.example.nhom3_tt_.dtos.response.EnrollResponse;
import com.example.nhom3_tt_.dtos.response.PageResponse;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.ErrorCode;
import com.example.nhom3_tt_.mappers.EnrollMapper;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Enroll;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.EnrollRepository;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.impl.EnrollServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class EnrollServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private CourseRepository courseRepository;

  @Mock private EnrollMapper enrollMapper;

  @Mock private EnrollRepository enrollRepository;
  @InjectMocks private EnrollServiceImpl enrollService;

  @BeforeAll
  static void beforeAll() {}

  private EnrollRequest enrollRequest;
  private Pageable pageable;
  private Long studentId;

  @BeforeEach
  void setUp() {
    enrollRequest = new EnrollRequest();
    enrollRequest.setStudentId(1L);
    enrollRequest.setCourseId(1L);

    pageable = PageRequest.of(0, 10);
    studentId = 1L;
  }

  @Test
  void findAll_valid_success() {
    // GIVEN
    List<Enroll> enrolls = Arrays.asList(new Enroll(), new Enroll(), new Enroll());
    Page<Enroll> enrollPage = new PageImpl<>(enrolls, pageable, enrolls.size());

    when(enrollRepository.findAll(pageable)).thenReturn(enrollPage);

    // WHEN
    PageResponse<?> result = enrollService.findAll(pageable);

    // THEN
    assertNotNull(result);
    assertEquals(0, result.getPageNO());
    assertEquals(10, result.getPageSize());
    assertEquals(1, result.getTotalPage());
    verify(enrollRepository).findAll(pageable);
    verify(enrollMapper, times(3)).convertToResponse(any(Enroll.class));
  }

  @Test
  void findById_enrollNotFound_fail() {
    // GIVEN
    Long id = 1L;
    when(enrollRepository.findById(id)).thenReturn(Optional.empty());

    // WHEN & THEN
    AppException exception = assertThrows(AppException.class, () -> enrollService.findById(id));
    assertEquals(ErrorCode.ENROLL_NOT_FOUND, exception.getErrorCode());
    verify(enrollRepository).findById(id);
  }

  @Test
  void enroll_studentAlreadyEnrolled_throwsException() {
    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(new Enroll()));

    AppException exception =
        assertThrows(AppException.class, () -> enrollService.enroll(enrollRequest));
    assertEquals(ErrorCode.STUDENT_ALREADY_ENROLLED, exception.getErrorCode());

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verifyNoMoreInteractions(enrollRepository, courseRepository, userRepository);
  }

  @Test
  void enroll_courseNotFound_throwsException() {
    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception =
        assertThrows(AppException.class, () -> enrollService.enroll(enrollRequest));
    assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verify(courseRepository).findById(1L);
    verifyNoMoreInteractions(enrollRepository, courseRepository, userRepository);
  }

  @Test
  void enroll_studentNotFound_throwsException() {
    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
    when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course()));
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception =
        assertThrows(AppException.class, () -> enrollService.enroll(enrollRequest));
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verify(courseRepository).findById(1L);
    verify(userRepository).findById(1L);
    verifyNoMoreInteractions(enrollRepository, courseRepository, userRepository);
  }

  @Test
  void enroll_success() {
    Course course = new Course();
    User user = new User();
    Enroll enroll = Enroll.builder().course(course).student(user).build();

    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(enrollRepository.save(any(Enroll.class))).thenReturn(enroll);

    Enroll result = enrollService.enroll(enrollRequest);

    assertNotNull(result);
    assertEquals(course, result.getCourse());
    assertEquals(user, result.getStudent());

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verify(courseRepository).findById(1L);
    verify(userRepository).findById(1L);
    verify(enrollRepository).save(any(Enroll.class));
  }

  @Test
  void findAllByStudentId_success() {
    Enroll enroll = new Enroll();
    EnrollResponse enrollResponse = new EnrollResponse();
    Page<Enroll> enrollPage = new PageImpl<>(List.of(enroll));
    List<EnrollResponse> enrollResponses = List.of(enrollResponse);

    when(enrollRepository.findAllByStudentId(studentId, pageable)).thenReturn(enrollPage);
    when(enrollMapper.convertToResponse(enroll)).thenReturn(enrollResponse);

    PageResponse<?> result = enrollService.findAllByStudentId(studentId, pageable);

    assertNotNull(result);
    assertEquals(enrollResponses, result.getItems());
    assertEquals(0, result.getPageNO());
    assertEquals(10, result.getPageSize());
    assertEquals(1, result.getTotalPage());

    verify(enrollRepository).findAllByStudentId(studentId, pageable);
    verify(enrollMapper).convertToResponse(enroll);
  }

  @Test
  void findAllByCourseId_success() {
    Enroll enroll = new Enroll();
    EnrollResponse enrollResponse = new EnrollResponse();
    Page<Enroll> enrollPage = new PageImpl<>(List.of(enroll));
    List<EnrollResponse> enrollResponses = List.of(enrollResponse);

    when(enrollRepository.findAllByCourseId(1L, pageable)).thenReturn(enrollPage);
    when(enrollMapper.convertToResponse(enroll)).thenReturn(enrollResponse);

    PageResponse<?> result = enrollService.findAllByCourseId(1L, pageable);

    assertNotNull(result);
    assertEquals(enrollResponses, result.getItems());
    assertEquals(0, result.getPageNO());
    assertEquals(10, result.getPageSize());
    assertEquals(1, result.getTotalPage());

    verify(enrollRepository).findAllByCourseId(1L, pageable);
    verify(enrollMapper).convertToResponse(enroll);
  }

  @Test
  void deleteEnrollById_enrollNotFound_throwsException() {
    when(enrollRepository.findById(1L)).thenReturn(Optional.empty());

    AppException exception =
        assertThrows(AppException.class, () -> enrollService.deleteEnrollById(1L));
    assertEquals(ErrorCode.ENROLL_NOT_FOUND, exception.getErrorCode());

    verify(enrollRepository).findById(1L);
    verifyNoMoreInteractions(enrollRepository);
  }

  @Test
  void deleteEnrollById_success() {
    Enroll enroll = new Enroll();
    when(enrollRepository.findById(1L)).thenReturn(Optional.of(enroll));

    enrollService.deleteEnrollById(1L);

    verify(enrollRepository).findById(1L);
    verify(enrollRepository).delete(enroll);
  }

  @Test
  void deleteEnroll_studentNotEnrolled_throwsException() {
    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());

    AppException exception =
        assertThrows(AppException.class, () -> enrollService.deleteEnroll(1L, 1L));
    assertEquals(ErrorCode.STUDENT_NOT_ENROLLED, exception.getErrorCode());

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verifyNoMoreInteractions(enrollRepository);
  }

  @Test
  void deleteEnroll_success() {
    Enroll enroll = new Enroll();
    when(enrollRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(enroll));

    enrollService.deleteEnroll(1L, 1L);

    verify(enrollRepository).findByStudentIdAndCourseId(1L, 1L);
    verify(enrollRepository).delete(enroll);
  }

  @Test
  void convertToEnrollResponsePage_success() {
    Enroll enroll = new Enroll();
    EnrollResponse enrollResponse = new EnrollResponse();
    Page<Enroll> enrollPage = new PageImpl<>(List.of(enroll));

    when(enrollMapper.convertToResponse(enroll)).thenReturn(enrollResponse);

    Page<EnrollResponse> result = enrollService.convertToEnrollResponsePage(enrollPage);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(enrollResponse, result.getContent().get(0));

    verify(enrollMapper).convertToResponse(enroll);
  }
}
