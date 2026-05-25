package com.example.nhom3_tt_.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.nhom3_tt_.dtos.requests.CourseRequest;
import com.example.nhom3_tt_.dtos.response.CourseResponse;
import com.example.nhom3_tt_.exception.CustomException;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.CourseMapper;
import com.example.nhom3_tt_.models.*;
import com.example.nhom3_tt_.repositories.CategoryRepository;
import com.example.nhom3_tt_.repositories.CourseRepository;
import com.example.nhom3_tt_.repositories.UserRepository;
import com.example.nhom3_tt_.services.CloudinaryService;
import com.example.nhom3_tt_.services.impl.CourseServiceImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
  @Mock private CourseRepository courseRepository;
  @Mock private UserRepository userRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private CourseMapper courseMapper;
  @Mock private CloudinaryService cloudinaryService;

  @InjectMocks private CourseServiceImpl courseService;

  //  @Test
  //  void uploadThumbnail_success() throws IOException {
  //    Long courseId = 1L;
  //    MultipartFile thumbnail = mock(MultipartFile.class);
  //    Course course = new Course();
  //    course.setId(courseId);
  //    CourseResponse response = new CourseResponse();
  //
  //    String thumbnailUrl = "https://example.com/thumbnail.jpg";
  //
  //    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
  //    when(cloudinaryService.uploadImage(thumbnail)).thenReturn(thumbnailUrl);
  //    when(courseRepository.save(any(Course.class))).thenReturn(course);
  //    when(courseMapper.convertToResponse(course)).thenReturn(response);
  //
  //    CourseResponse result = courseService.uploadThumbnail(courseId, thumbnail);
  //
  //    assertNotNull(result);
  //    assertEquals(response, result);
  //    assertEquals(thumbnailUrl, course.getThumbnail());
  //
  //    verify(courseRepository).findById(courseId);
  //    verify(cloudinaryService).uploadImage(thumbnail);
  //    verify(courseRepository).save(course);
  //    verify(courseMapper).convertToResponse(course);
  //  }

  //  @Test
  //  void uploadThumbnail_notFound() {
  //    Long courseId = 1L;
  //    MultipartFile thumbnail = mock(MultipartFile.class);
  //
  //    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());
  //
  //    NotFoundException exception =
  //        assertThrows(
  //            NotFoundException.class,
  //            () -> {
  //              courseService.uploadThumbnail(courseId, thumbnail);
  //            });
  //
  //    assertEquals("No course ID found: " + courseId, exception.getMessage());
  //
  //    verify(courseRepository).findById(courseId);
  //    verifyNoInteractions(cloudinaryService);
  //    verifyNoMoreInteractions(courseRepository);
  //  }

  //  @Test
  //  void uploadIntroVideo_success() throws IOException {
  //    Long courseId = 1L;
  //    MultipartFile video = mock(MultipartFile.class);
  //    Course course = new Course();
  //    course.setId(courseId);
  //    CourseResponse response = new CourseResponse();
  //
  //    String videoUrl = "https://example.com/video.mp4";
  //
  //    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
  //    when(cloudinaryService.uploadVideo(video)).thenReturn(videoUrl);
  //    when(courseRepository.save(any(Course.class))).thenReturn(course);
  //    when(courseMapper.convertToResponse(course)).thenReturn(response);
  //
  //    CourseResponse result = courseService.uploadVideo(courseId, video);
  //
  //    assertNotNull(result);
  //    assertEquals(response, result);
  //    assertEquals(videoUrl, course.getIntroVideo());
  //
  //    verify(courseRepository).findById(courseId);
  //    verify(cloudinaryService).uploadVideo(video);
  //    verify(courseRepository).save(course);
  //    verify(courseMapper).convertToResponse(course);
  //  }

  //  @Test
  //  void uploadIntroVideo_notFound() {
  //    Long courseId = 1L;
  //    MultipartFile video = mock(MultipartFile.class);
  //
  //    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());
  //
  //    NotFoundException exception =
  //        assertThrows(
  //            NotFoundException.class,
  //            () -> {
  //              courseService.uploadVideo(courseId, video);
  //            });
  //
  //    assertEquals("No course ID found: " + courseId, exception.getMessage());
  //
  //    verify(courseRepository).findById(courseId);
  //    verifyNoInteractions(cloudinaryService);
  //    verifyNoMoreInteractions(courseRepository);
  //  }

  @Test
  void createCourse_success() {
    CourseRequest request = new CourseRequest();
    request.setRegularPrice(100);
    request.setInstructorId(1L);
    request.setCategoryId(1L);

    User instructor = new User();
    Category category = new Category();
    Course course = new Course();
    CourseResponse response = new CourseResponse();

    when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(courseMapper.convertToEntity(request)).thenReturn(course);
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.create(request);

    assertNotNull(result);
    assertEquals(response, result);

    verify(userRepository).findById(1L);
    verify(categoryRepository).findById(1L);
    verify(courseRepository).save(course);
    verify(courseMapper).convertToResponse(course);
  }

  @Test
  void createCourse_invalidPrice_throwsException() {
    CourseRequest request = new CourseRequest();
    request.setRegularPrice(100);

    assertThrows(NotFoundException.class, () -> courseService.create(request));
  }

  @Test
  void create_withInvalidInstructorId_shouldThrowException() {
    CourseRequest request = new CourseRequest();
    request.setRegularPrice(100);
    request.setInstructorId(99L);

    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.create(request));
    assertEquals("Instructor not found with id=99", exception.getMessage());
    verify(userRepository).findById(99L);
  }

  @Test
  void create_withInvalidCategoryId_shouldThrowException() {
    CourseRequest request = new CourseRequest();
    request.setRegularPrice(100);
    request.setInstructorId(1L); // Đảm bảo instructorId hợp lệ
    request.setCategoryId(99L); // Category không hợp lệ

    User instructor = new User();
    when(userRepository.findById(1L)).thenReturn(Optional.of(instructor)); // Instructor tồn tại
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty()); // Category không tồn tại

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.create(request));
    assertEquals("Category not found with id=99", exception.getMessage());
    verify(userRepository).findById(1L);
    verify(categoryRepository).findById(99L);
  }

  @Test
  void create_withValidRequest_shouldReturnCourseResponse() {
    CourseRequest request = new CourseRequest();
    request.setRegularPrice(100);
    request.setInstructorId(1L);
    request.setCategoryId(1L);

    User instructor = new User();
    Category category = new Category();
    Course course = new Course();
    CourseResponse response = new CourseResponse();

    when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
    when(courseMapper.convertToEntity(request)).thenReturn(course);
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.create(request);

    assertNotNull(result);
    assertEquals(response, result);
    verify(userRepository).findById(1L);
    verify(categoryRepository).findById(1L);
    verify(courseRepository).save(course);
    verify(courseMapper).convertToResponse(course);
  }

  @Test
  void getCoursesByCategory_success() {
    Category category = new Category();
    Course course = new Course();
    when(courseRepository.findByCategory(category)).thenReturn(List.of(course));

    List<Course> result = courseService.getCoursesByCategory(category);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findByCategory(category);
  }

  @Test
  void getCoursesByCategory_emptyList() {
    Category category = new Category();
    when(courseRepository.findByCategory(category)).thenReturn(List.of());

    List<Course> result = courseService.getCoursesByCategory(category);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(courseRepository).findByCategory(category);
  }

  @Test
  void getCoursesByLevel_success() {
    String level = "BEGINNER";
    Pageable pageable = Pageable.unpaged();
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    Page<Course> page = mock(Page.class);

    when(courseRepository.findByLevel(ETypeLevel.BEGINNER, pageable)).thenReturn(page);
    when(page.stream()).thenReturn(List.of(course).stream());
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.getCoursesByLevel(level, pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findByLevel(ETypeLevel.BEGINNER, pageable);
    verify(courseMapper).convertToResponse(course);
  }

  @Test
  void getCoursesByLevel_invalidLevel_throwsException() {
    String level = "INVALID";

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> courseService.getCoursesByLevel(level, Pageable.unpaged()));

    assertEquals("Invalid level: 'INVALID", exception.getMessage());
  }

  @Test
  void getCoursesByCategoryId_success() {
    Long categoryId = 1L;
    Pageable pageable = Pageable.unpaged();
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    Page<Course> page = mock(Page.class);

    when(courseRepository.findByCategoryId(categoryId, pageable)).thenReturn(page);
    when(page.stream()).thenReturn(List.of(course).stream());
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.getCoursesByCategoryId(categoryId, pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findByCategoryId(categoryId, pageable);
  }

  @Test
  void getById_success() {
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.getById(1L);

    assertNotNull(result);
    assertEquals(response, result);
    verify(courseRepository).findById(1L);
  }

  @Test
  void getById_notFound_throwsException() {
    when(courseRepository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.getById(1L));
    assertEquals("Course is not found", exception.getMessage());

    verify(courseRepository).findById(1L);
  }

  @Test
  void getCourseByInstructorId_success() {
    Long instructorId = 1L;
    Pageable pageable = Pageable.unpaged();
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    Page<Course> page = mock(Page.class);

    when(courseRepository.findByInstructorId(instructorId, pageable)).thenReturn(page);
    when(page.stream()).thenReturn(List.of(course).stream());
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.getCourseByIntructorId(instructorId, pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findByInstructorId(instructorId, pageable);
  }

  @Test
  void searchByTitle_success() {
    String title = "Java";
    Pageable pageable = Pageable.unpaged();
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    Page<Course> page = mock(Page.class);

    when(courseRepository.findByTitleContainingIgnoreCase("Java", pageable)).thenReturn(page);
    when(page.stream()).thenReturn(List.of(course).stream());
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.searchByTitle(title, pageable);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findByTitleContainingIgnoreCase("Java", pageable);
  }

  @Test
  void getByIdEntity_success() {
    Long id = 1L;
    Course course = new Course();

    when(courseRepository.findById(id)).thenReturn(Optional.of(course));

    Course result = courseService.getByIdEntity(id);

    assertNotNull(result);
    assertEquals(course, result);
    verify(courseRepository).findById(id);
  }

  @Test
  void getByIdEntity_notFound_throwsException() {
    Long id = 1L;

    when(courseRepository.findById(id)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.getByIdEntity(id));

    assertEquals("Course is not found", exception.getMessage());
  }

  @Test
  void getAllCourses_success() {
    Course course = new Course();
    CourseResponse response = new CourseResponse();

    when(courseRepository.findAll()).thenReturn(List.of(course));
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(courseRepository).findAll();
  }

  @Test
  void getAll_withEmptyPageable_shouldReturnEmptyList() {
    Page<Course> emptyPage = Page.empty();
    when(courseRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

    List<CourseResponse> result = courseService.getAll(Pageable.unpaged());

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(courseRepository).findAll(any(Pageable.class));
  }

  @Test
  void getAll_withNonEmptyPageable_shouldReturnList() {
    Course course = new Course();
    CourseResponse response = new CourseResponse();
    Page<Course> page = mock(Page.class);

    when(courseRepository.findAll(any(Pageable.class))).thenReturn(page);
    when(page.stream()).thenReturn(List.of(course).stream());
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    List<CourseResponse> result = courseService.getAll(Pageable.unpaged());

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(response, result.get(0));
    verify(courseRepository).findAll(any(Pageable.class));
    verify(courseMapper).convertToResponse(course);
  }

  //  @Test
  //  void update_withInvalidId_shouldThrowException() {
  //    CourseRequest request = new CourseRequest();
  //    request.setRegularPrice(100);
  //
  //    when(courseRepository.findById(99L)).thenReturn(Optional.empty());
  //
  //
  //    NotFoundException exception =
  //        assertThrows(NotFoundException.class, () -> courseService.update(99L, request));
  //    assertEquals("Course is not found", exception.getMessage());
  //    verify(courseRepository).findById(99L);
  //  }

  //  @Test
  //  void forceDelete_withInvalidId_shouldThrowException() {
  //    when(courseRepository.findById(99L)).thenReturn(Optional.empty());
  //
  //    NotFoundException exception =
  //        assertThrows(NotFoundException.class, () -> courseService.forceDelete(99L));
  //    assertEquals("Course is not found", exception.getMessage());
  //    verify(courseRepository).findById(99L);
  //  }

  @Test
  void approveCourse_withInvalidId_shouldThrowException() {
    when(courseRepository.findById(99L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.approveCourse(99L));
    assertEquals("No course ID found: 99", exception.getMessage());
    verify(courseRepository).findById(99L);
  }

  //  @Test
  //  void updateCourse_success() {
  //    // Setup mock input data
  //    CourseRequest request = new CourseRequest();
  //    request.setRegularPrice(100);
  //    request.setInstructorId(2L);
  //    request.setCategoryId(3L);
  //
  //    Course course = new Course();
  //    User instructor = new User(); // Mock instructor
  //    Category category = new Category(); // Mock category
  //    CourseResponse response = new CourseResponse();
  //
  //    // Mock repository and mapper behavior
  //    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
  //    when(userRepository.findById(2L)).thenReturn(Optional.of(instructor));
  //    when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
  //    when(courseRepository.save(course)).thenReturn(course);
  //    when(courseMapper.convertToResponse(course)).thenReturn(response);
  //
  //    // Act: Call the service method
  //    CourseResponse result = courseService.update(1L, request);
  //
  //    // Assert: Verify the output and interactions
  //    assertNotNull(result);
  //    assertEquals(response, result);
  //
  //    verify(courseRepository).findById(1L);
  //    verify(userRepository).findById(2L);
  //    verify(categoryRepository).findById(3L);
  //    verify(courseMapper).updateCourseFromRequest(request, course);
  //    verify(courseRepository).save(course);
  //    verify(courseMapper).convertToResponse(course);
  //  }

  //  @Test
  //  void updateCourse_instructorNotFound() {
  //    // Setup mock input data
  //    CourseRequest request = new CourseRequest();
  //    request.setInstructorId(2L);
  //
  //    Course course = new Course();
  //
  //    // Mock repository behavior
  //    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
  //    when(userRepository.findById(2L)).thenReturn(Optional.empty());
  //
  //    // Act & Assert: Verify exception is thrown
  //    NotFoundException exception =
  //        assertThrows(NotFoundException.class, () -> courseService.update(1L, request));
  //    assertEquals("Instructor cannot found with id = 2", exception.getMessage());
  //
  //    verify(courseRepository).findById(1L);
  //    verify(userRepository).findById(2L);
  //    verifyNoInteractions(categoryRepository);
  //    verifyNoInteractions(courseMapper);
  //    verifyNoMoreInteractions(courseRepository);
  //  }

  //  @Test
  //  void updateCourse_categoryNotFound() {
  //    // Setup mock input data
  //    CourseRequest request = new CourseRequest();
  //    request.setInstructorId(2L);
  //    request.setCategoryId(3L);
  //
  //    Course course = new Course();
  //    User instructor = new User();
  //
  //    // Mock repository behavior
  //    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
  //    when(userRepository.findById(2L)).thenReturn(Optional.of(instructor));
  //    when(categoryRepository.findById(3L)).thenReturn(Optional.empty());
  //
  //    // Act & Assert: Verify exception is thrown
  //    NotFoundException exception =
  //        assertThrows(NotFoundException.class, () -> courseService.update(1L, request));
  //    assertEquals("Category cannot found with id = 3", exception.getMessage());
  //
  //    verify(courseRepository).findById(1L);
  //    verify(userRepository).findById(2L);
  //    verify(categoryRepository).findById(3L);
  //    verifyNoInteractions(courseMapper);
  //    verifyNoMoreInteractions(courseRepository);
  //  }

  //  @Test
  //  void deleteCourse_success() {
  //    // Arrange
  //    Course course = new Course();
  //    when(courseRepository.findById(1L)).thenReturn(Optional.of(course)); // Stub findById
  //    doNothing().when(courseRepository).deleteById(1L); // Stub deleteById
  //
  //    // Act
  //    courseService.forceDelete(1L);
  //
  //    // Assert
  //    verify(courseRepository).findById(1L); // Verify findById được gọi
  //    verify(courseRepository).deleteById(1L); // Verify deleteById được gọi
  //  }

  @Test
  void approveCourse_success() {
    Course course = new Course();
    course.setStatus(ECourseStatus.PENDING);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.approveCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.APPROVED, course.getStatus());
    verify(courseRepository).save(course);
  }

  @Test
  void rejectCourse_success() {
    Course course = new Course();
    course.setStatus(ECourseStatus.PENDING);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.rejectCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.REJECT, course.getStatus());
    verify(courseRepository).save(course);
  }

  @Test
  void rejectCourse_withInvalidId_shouldThrowException() {
    when(courseRepository.findById(99L)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> courseService.rejectCourse(99L));
    assertEquals("No course ID found: 99", exception.getMessage());
    verify(courseRepository).findById(99L);
  }

  @Test
  void approveCourse_withApprovedStatus_shouldThrowException() {
    Course course = new Course();
    course.setStatus(ECourseStatus.APPROVED);

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

    CustomException exception =
        assertThrows(CustomException.class, () -> courseService.approveCourse(1L));
    assertEquals("The course has been previously approved", exception.getMessage());

    verify(courseRepository).findById(1L);
  }

  @Test
  void approveCourse_withRejectedStatus_shouldApprove() {
    Course course = new Course();
    course.setStatus(ECourseStatus.REJECT);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.approveCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.APPROVED, course.getStatus());
    verify(courseRepository).findById(1L);
    verify(courseRepository).save(course);
  }

  @Test
  void approveCourse_withPendingStatus_shouldApprove() {
    Course course = new Course();
    course.setStatus(ECourseStatus.PENDING);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.approveCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.APPROVED, course.getStatus());
    verify(courseRepository).findById(1L);
    verify(courseRepository).save(course);
  }

  @Test
  void rejectCourse_withRejectedStatus_shouldThrowException() {
    Course course = new Course();
    course.setStatus(ECourseStatus.REJECT);

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

    CustomException exception =
        assertThrows(CustomException.class, () -> courseService.rejectCourse(1L));
    assertEquals("The course has been rejected before", exception.getMessage());

    verify(courseRepository).findById(1L);
  }

  @Test
  void rejectCourse_withApprovedStatus_shouldReject() {
    Course course = new Course();
    course.setStatus(ECourseStatus.APPROVED);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.rejectCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.REJECT, course.getStatus());
    verify(courseRepository).findById(1L);
    verify(courseRepository).save(course);
  }

  @Test
  void rejectCourse_withPendingStatus_shouldReject() {
    Course course = new Course();
    course.setStatus(ECourseStatus.PENDING);
    CourseResponse response = new CourseResponse();

    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(course);
    when(courseMapper.convertToResponse(course)).thenReturn(response);

    CourseResponse result = courseService.rejectCourse(1L);

    assertNotNull(result);
    assertEquals(ECourseStatus.REJECT, course.getStatus());
    verify(courseRepository).findById(1L);
    verify(courseRepository).save(course);
  }

  @Test
  void testGetAllApproved() {
    // Mock input
    Pageable pageable = PageRequest.of(0, 10);
    Course course1 = new Course();
    Course course2 = new Course();
    List<Course> courses = Arrays.asList(course1, course2);

    CourseResponse courseResponse1 = new CourseResponse();
    CourseResponse courseResponse2 = new CourseResponse();

    // Mock repository and mapper methods
    when(courseRepository.findByStatus(ECourseStatus.APPROVED, pageable))
        .thenReturn(new PageImpl<>(courses)); // Mock Page of courses
    when(courseMapper.convertToResponse(course1)).thenReturn(courseResponse1);
    when(courseMapper.convertToResponse(course2)).thenReturn(courseResponse2);

    // Call the service method
    List<CourseResponse> result = courseService.getAllApproved(pageable);

    // Assert the result
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(courseRepository).findByStatus(ECourseStatus.APPROVED, pageable);
  }

  @Test
  void testGetAllPending() {
    // Mock input
    Pageable pageable = PageRequest.of(0, 10);
    Course course1 = new Course();
    Course course2 = new Course();
    List<Course> courses = Arrays.asList(course1, course2);

    CourseResponse courseResponse1 = new CourseResponse();
    CourseResponse courseResponse2 = new CourseResponse();

    // Mock repository and mapper methods
    when(courseRepository.findByStatus(ECourseStatus.PENDING, pageable))
        .thenReturn(new PageImpl<>(courses)); // Mock Page of courses
    when(courseMapper.convertToResponse(course1)).thenReturn(courseResponse1);
    when(courseMapper.convertToResponse(course2)).thenReturn(courseResponse2);

    // Call the service method
    List<CourseResponse> result = courseService.getAllPending(pageable);

    // Assert the result
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(courseRepository).findByStatus(ECourseStatus.PENDING, pageable);
  }

  @Test
  void testGetAllReject() {
    // Mock input
    Pageable pageable = PageRequest.of(0, 10);
    Course course1 = new Course();
    Course course2 = new Course();
    List<Course> courses = Arrays.asList(course1, course2);

    CourseResponse courseResponse1 = new CourseResponse();
    CourseResponse courseResponse2 = new CourseResponse();

    // Mock repository and mapper methods
    when(courseRepository.findByStatus(ECourseStatus.REJECT, pageable))
        .thenReturn(new PageImpl<>(courses)); // Mock Page of courses
    when(courseMapper.convertToResponse(course1)).thenReturn(courseResponse1);
    when(courseMapper.convertToResponse(course2)).thenReturn(courseResponse2);

    // Call the service method
    List<CourseResponse> result = courseService.getAllReject(pageable);

    // Assert the result
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(courseRepository).findByStatus(ECourseStatus.REJECT, pageable);
  }
}
