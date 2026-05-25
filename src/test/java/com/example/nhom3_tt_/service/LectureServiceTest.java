package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.requests.lecture.LectureRequest;
import com.example.nhom3_tt_.dtos.requests.lecture.LectureVideoFolderStructure;
import com.example.nhom3_tt_.dtos.response.lecture.LectureResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.LectureMapper;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Lecture;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.LectureRepository;
import com.example.nhom3_tt_.services.CloudinaryService;
import com.example.nhom3_tt_.services.CourseService;
import com.example.nhom3_tt_.services.SectionService;
import com.example.nhom3_tt_.services.impl.LectureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class LectureServiceTest {

  @Mock private LectureRepository repository;
  @Mock private LectureMapper mapper;
  @InjectMocks private LectureServiceImpl lectureService;
  @Mock private SectionService sectionService;
  @Mock private CourseService courseService;
  @Mock private CloudinaryService cloudinaryService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedInstructor(Long instructorId) {
    User instructor = new User();
    instructor.setId(instructorId);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(instructor, null));
  }

  @Test
  void create_success() {
    Lecture lecture = new Lecture();
    LectureResponse lectureResponse = new LectureResponse();
    when(repository.save(lecture)).thenReturn(lecture);
    when(mapper.toLectureResponse(lecture)).thenReturn(lectureResponse);

    LectureResponse result = lectureService.create(lecture);

    assertNotNull(result);
    assertEquals(lectureResponse, result);

    verify(repository).save(lecture);
    verify(mapper).toLectureResponse(lecture);
  }

  @Test
  void createLecture_success() throws IOException {
    LectureRequest request = new LectureRequest();
    request.setSectionId(1L);
    MultipartFile video = mock(MultipartFile.class);
    MultipartFile thumbnail = mock(MultipartFile.class);
    Lecture lecture = new Lecture();
    LectureResponse lectureResponse = new LectureResponse();

    Section section = new Section();
    Course course = new Course();
    User instructor = new User();
    instructor.setId(1L);
    course.setId(1L);
    course.setInstructor(instructor);
    section.setCourse(course);
    setAuthenticatedInstructor(1L);

    when(mapper.toLecture(request)).thenReturn(lecture);
    when(cloudinaryService.uploadVideo(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenReturn("videoUrl");
    when(cloudinaryService.uploadImage(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenReturn("thumbnailUrl");
    when(repository.save(lecture)).thenReturn(lecture);
    when(mapper.toLectureResponse(lecture)).thenReturn(lectureResponse);
    when(sectionService.getEntityById(1L)).thenReturn(section);
    when(courseService.getByIdEntity(1L)).thenReturn(course);

    LectureResponse result = lectureService.createLecture(request, video, thumbnail);

    assertNotNull(result);
    assertEquals(lectureResponse, result);

    verify(mapper).toLecture(request);
    verify(cloudinaryService)
        .uploadVideo(any(MultipartFile.class), any(LectureVideoFolderStructure.class));
    verify(cloudinaryService)
        .uploadImage(any(MultipartFile.class), any(LectureVideoFolderStructure.class));
    verify(repository).save(lecture);
    verify(mapper).toLectureResponse(lecture);
  }

  //  @Test
  //  void createLecture_sectionNotFound_throwsNotFoundException() {
  //    LectureRequest request = new LectureRequest();
  //    request.setSectionId(1L);
  //    MultipartFile video = mock(MultipartFile.class);
  //    MultipartFile thumbnail = mock(MultipartFile.class);
  //
  //    when(sectionService.getEntityById(1L)).thenReturn(null);
  //
  //    assertThrows(
  //        NotFoundException.class,
  //        () -> {
  //          lectureService.createLecture(request, video, thumbnail);
  //        });
  //  }

  @Test
  void createLecture_uploadFailure_throwsRuntimeException() throws IOException {
    LectureRequest request = new LectureRequest();
    request.setSectionId(1L);
    MultipartFile video = mock(MultipartFile.class);
    MultipartFile thumbnail = mock(MultipartFile.class);
    Lecture lecture = new Lecture();

    Section section = new Section();
    Course course = new Course();
    User instructor = new User();
    instructor.setId(1L);
    course.setId(1L);
    course.setInstructor(instructor);
    section.setCourse(course);

    when(mapper.toLecture(request)).thenReturn(lecture);
    when(sectionService.getEntityById(1L)).thenReturn(section);
    when(courseService.getByIdEntity(1L)).thenReturn(course);
    when(cloudinaryService.uploadVideo(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenThrow(new IOException("Upload failed"));
    setAuthenticatedInstructor(1L);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              lectureService.createLecture(request, video, thumbnail);
            });

    assertEquals("Failed to upload video or thumbnail", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }

  //  @Test
  //  void createLecture_courseNotFound_throwsNotFoundException() {
  //    LectureRequest request = new LectureRequest();
  //    request.setSectionId(1L);
  //    MultipartFile video = mock(MultipartFile.class);
  //    MultipartFile thumbnail = mock(MultipartFile.class);
  //    Section section = new Section();
  //    section.setCourse(null);
  //
  //    when(sectionService.getEntityById(1L)).thenReturn(section);
  //
  //    assertThrows(
  //        NotFoundException.class,
  //        () -> {
  //          lectureService.createLecture(request, video, thumbnail);
  //        });
  //  }

  @Test
  void updateLecture_success() throws IOException {
    Long id = 1L;
    LectureRequest request = new LectureRequest();
    request.setSectionId(1L);
    MultipartFile video = mock(MultipartFile.class);
    MultipartFile thumbnail = mock(MultipartFile.class);
    Lecture lecture = new Lecture();
    LectureResponse lectureResponse = new LectureResponse();

    Section section = new Section();
    Course course = new Course();
    User instructor = new User();
    instructor.setId(1L);
    course.setId(1L);
    course.setInstructor(instructor);
    section.setCourse(course);

    doReturn(Optional.of(lecture)).when(repository).findById(id);
    when(mapper.updateLecture(request, lecture)).thenReturn(lecture);
    when(cloudinaryService.uploadVideo(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenReturn("videoUrl");
    when(cloudinaryService.uploadImage(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenReturn("thumbnailUrl");
    when(lectureService.update(lecture)).thenReturn(lectureResponse);
    when(sectionService.getEntityById(1L)).thenReturn(section);
    when(courseService.getByIdEntity(1L)).thenReturn(course);
    setAuthenticatedInstructor(1L);

    LectureResponse result = lectureService.updateLecture(id, request, video, thumbnail);

    assertNotNull(result);
    assertEquals(lectureResponse, result);

    verify(repository).findById(id);
    verify(mapper).updateLecture(request, lecture);
    verify(cloudinaryService)
        .uploadVideo(any(MultipartFile.class), any(LectureVideoFolderStructure.class));
    verify(cloudinaryService)
        .uploadImage(any(MultipartFile.class), any(LectureVideoFolderStructure.class));
    //    verify(lectureService).update(lecture);
  }

  //  @Test
  //  void updateLecture_sectionNotFound_throwsNotFoundException() {
  //    Long id = 1L;
  //    LectureRequest request = new LectureRequest();
  //    request.setSectionId(1L);
  //    MultipartFile video = mock(MultipartFile.class);
  //    MultipartFile thumbnail = mock(MultipartFile.class);
  //    Lecture lecture = new Lecture();
  //
  //    doReturn(Optional.of(lecture)).when(repository).findById(id);
  //    when(sectionService.getEntityById(1L)).thenReturn(null);
  //
  //    assertThrows(
  //        NotFoundException.class,
  //        () -> {
  //          lectureService.updateLecture(id, request, video, thumbnail);
  //        });
  //  }

  @Test
  void updateLecture_uploadFailure_throwsRuntimeException() throws IOException {
    Long id = 1L;
    LectureRequest request = new LectureRequest();
    request.setSectionId(1L);
    MultipartFile video = mock(MultipartFile.class);
    MultipartFile thumbnail = mock(MultipartFile.class);
    Lecture lecture = new Lecture();

    Section section = new Section();
    Course course = new Course();
    User instructor = new User();
    instructor.setId(1L);
    course.setId(1L);
    course.setInstructor(instructor);
    section.setCourse(course);
    lecture.setSection(section);
    doReturn(Optional.of(lecture)).when(repository).findById(id);
    when(mapper.updateLecture(request, lecture)).thenReturn(lecture);
    when(sectionService.getEntityById(1L)).thenReturn(section);
    when(courseService.getByIdEntity(1L)).thenReturn(course);
    when(cloudinaryService.uploadVideo(
            any(MultipartFile.class), any(LectureVideoFolderStructure.class)))
        .thenThrow(new IOException("Upload failed"));
    setAuthenticatedInstructor(1L);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              lectureService.updateLecture(id, request, video, thumbnail);
            });

    assertEquals("Failed to upload video or thumbnail", exception.getMessage());
    assertInstanceOf(IOException.class, exception.getCause());
  }

  @Test
  void getAllBySectionId_success() {
    Long sectionId = 1L;
    Lecture lecture = new Lecture();
    LectureResponse lectureResponse = new LectureResponse();

    when(repository.findAllBySectionId(sectionId)).thenReturn(List.of(lecture));
    when(mapper.toLectureResponse(lecture)).thenReturn(lectureResponse);

    List<LectureResponse> result = lectureService.getAllBySectionId(sectionId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(lectureResponse, result.get(0));

    verify(repository).findAllBySectionId(sectionId);
    verify(mapper).toLectureResponse(lecture);
  }

  @Test
  void getAll_success() {
    Lecture lecture = new Lecture();
    LectureResponse lectureResponse = new LectureResponse();
    when(repository.findAll()).thenReturn(List.of(lecture));
    when(mapper.toLectureResponse(lecture)).thenReturn(lectureResponse);

    List<LectureResponse> result = lectureService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(lectureResponse, result.get(0));

    verify(repository).findAll();
    verify(mapper).toLectureResponse(lecture);
  }

  @Test
  void getById_success() {
    Long id = 1L;
    Lecture lecture = new Lecture();
    lecture.setId(1L);
    lecture.setTitle("Test Lecture");

    LectureResponse lectureResponse = new LectureResponse();
    lectureResponse.setId(1L);
    lectureResponse.setTitle("Test Lecture");

    when(repository.existsById(1L)).thenReturn(true);
    when(repository.findById(1L)).thenReturn(Optional.of(lecture));
    when(mapper.toLectureResponse(lecture)).thenReturn(lectureResponse);

    LectureResponse result = lectureService.getById(id);

    // Assert: kiểm tra kết quả trả về
    assertNotNull(result);
    assertEquals(lectureResponse.getId(), result.getId());
    assertEquals(lectureResponse.getTitle(), result.getTitle());

    verify(repository).existsById(id);
    verify(repository).findById(id);
    verify(mapper).toLectureResponse(lecture);
  }

  @Test
  void getById_notFound_throwsNotFoundException() {
    when(repository.existsById(1L)).thenReturn(false);
    //    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> {
          lectureService.getById(1L);
        });

    verify(repository).existsById(1L);
  }

  //  @Test
  //  void delete_success() {
  //    Long id = 1L;
  //    Lecture lecture = new Lecture();
  //    when(repository.existsById(id)).thenReturn(true);
  //    doNothing().when(repository).deleteById(id);
  //
  //    lectureService.delete(id);
  //
  //    verify(repository).existsById(id);
  //    verify(repository).deleteById(id);
  //  }

  @Test
  void testLectureVideoFolderStructure() {
    LectureVideoFolderStructure folderStructure =
        LectureVideoFolderStructure.builder().instructorId(1L).courseId(1L).sectionId(1L).build();

    assertNotNull(folderStructure);
    assertEquals(1L, folderStructure.getInstructorId());
    assertEquals(1L, folderStructure.getCourseId());
    assertEquals(1L, folderStructure.getSectionId());
  }
}
