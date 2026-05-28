package com.example.nhom3_tt_.services.impl;

import com.example.nhom3_tt_.dtos.requests.lecture.LectureRequest;
import com.example.nhom3_tt_.dtos.requests.lecture.LectureVideoFolderStructure;
import com.example.nhom3_tt_.dtos.response.lecture.LectureResponse;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.exception.ErrorCode;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.LectureMapper;
import com.example.nhom3_tt_.models.Course;
import com.example.nhom3_tt_.models.Lecture;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.LectureRepository;
import com.example.nhom3_tt_.services.CloudinaryService;
import com.example.nhom3_tt_.services.CourseService;
import com.example.nhom3_tt_.services.LectureService;
import com.example.nhom3_tt_.services.SectionService;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

  private final LectureRepository repository;
  private final LectureMapper mapper;
  private final SectionService sectionService;
  private final CourseService courseService;
  private final CloudinaryService cloudinaryService;

  @Value("${default.thumbnail.url}")
  private String defaultThumbnailUrl;

  @Override
  public LectureResponse create(Lecture lecture) {
    return mapper.toLectureResponse(repository.save(lecture));
  }

  @Override
  public LectureResponse createLecture(
      LectureRequest request, MultipartFile video, MultipartFile thumbnail) {

    if (!Objects.equals(
        sectionService.getEntityById(request.getSectionId()).getCourse().getInstructor().getId(),
        ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())) {
      throw new AppException(ErrorCode.NOT_INSTRUCTOR);
    }

    Lecture lecture = mapper.toLecture(request);
    String videoUrl = null;
    String thumbnailUrl;

    try {
      LectureVideoFolderStructure folderStructure = createFolderStructure(request.getSectionId());

      // Upload video nếu không null
      if (video != null && !video.isEmpty()) {
        videoUrl = cloudinaryService.uploadVideo(video, folderStructure);
      }

      // Upload thumbnail hoặc dùng URL mặc định nếu null
      if (thumbnail != null && !thumbnail.isEmpty()) {
        thumbnailUrl = cloudinaryService.uploadImage(thumbnail, folderStructure);
      } else {
        thumbnailUrl = defaultThumbnailUrl;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload video or thumbnail", e);
    }

    lecture.setLinkVideo(videoUrl);
    lecture.setThumbnail(thumbnailUrl);

    return create(lecture);
  }

  @Override
  public LectureResponse updateLecture(
      Long id, LectureRequest request, MultipartFile video, MultipartFile thumbnail) {
    if (!Objects.equals(
        sectionService.getEntityById(request.getSectionId()).getCourse().getInstructor().getId(),
        ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())) {
      throw new AppException("You are not the instructor of this course");
    }
    Lecture lecture = getEntityById(id);
    if (lecture == null) {
      throw new NotFoundException("Lecture cannot found with id = " + id);
    }

    mapper.updateLecture(request, lecture);

    try {
      Long sectionId =
          (request != null && request.getSectionId() != null)
              ? request.getSectionId()
              : lecture.getSection().getId();

      LectureVideoFolderStructure folderStructure = createFolderStructure(sectionId);

      // Upload video nếu không null
      if (video != null && !video.isEmpty()) {
        String videoUrl = cloudinaryService.uploadVideo(video, folderStructure);
        lecture.setLinkVideo(videoUrl);
      }

      // Upload thumbnail hoặc dùng URL mặc định nếu thumbnail null
      if (thumbnail != null && !thumbnail.isEmpty()) {
        String thumbnailUrl = cloudinaryService.uploadImage(thumbnail, folderStructure);
        lecture.setThumbnail(thumbnailUrl);
      } else {
        lecture.setThumbnail(defaultThumbnailUrl);
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to upload video or thumbnail", e);
    }

    return update(lecture);
  }

  @Override
  public List<LectureResponse> getAllBySectionId(Long sectionId) {
    return repository.findAllBySectionId(sectionId).stream()
        .map(mapper::toLectureResponse)
        .toList();
  }

  @Override
  public LectureResponse update(Lecture lecture) {
    return mapper.toLectureResponse(repository.save(lecture));
  }

  @Override
  public List<LectureResponse> getAll() {
    return repository.findAll().stream().map(mapper::toLectureResponse).toList();
  }

  @Override
  public LectureResponse getById(Long id) {
    boolean isExistedLecture = repository.existsById(id);
    if (!isExistedLecture) {
      throw new NotFoundException("Lecture not found with id = " + id);
    }
    return mapper.toLectureResponse(repository.findById(id).orElse(null));
  }

  @Override
  public Lecture getEntityById(Long id) {
    return repository.findById(id).orElse(null);
  }

  @Override
  public void delete(Long id) {
    Lecture lecture = repository.findById(id).orElse(null);
    if (lecture == null) {
      throw new NotFoundException("Lecture not found with id = " + id);
    }

    Authentication auth = SecurityContextHolder.getContext() == null ? null : SecurityContextHolder.getContext().getAuthentication();
    Object principal = auth == null ? null : auth.getPrincipal();
    Long principalId = null;
    if (principal instanceof User) {
      principalId = ((User) principal).getId();
    }

    Long instructorId = lecture.getSection() == null || lecture.getSection().getCourse() == null
        ? null
        : lecture.getSection().getCourse().getInstructor() == null
            ? null
            : lecture.getSection().getCourse().getInstructor().getId();

    if (!Objects.equals(instructorId, principalId)) {
      throw new AppException(ErrorCode.NOT_INSTRUCTOR);
    }

    boolean isExistedLecture = repository.existsById(id);
    if (!isExistedLecture) {
      throw new NotFoundException("Lecture not found with id = " + id);
    }

    repository.deleteById(id);
  }

  private LectureVideoFolderStructure createFolderStructure(Long sectionId) {
    Section section = sectionService.getEntityById(sectionId);
    if (section == null
        || section.getCourse() == null
        || section.getCourse().getInstructor() == null) {
      throw new NotFoundException("Section not found");
    }
    Course course = courseService.getByIdEntity(section.getCourse().getId());
    Long instructorId = course.getInstructor().getId();
    return LectureVideoFolderStructure.builder()
        .instructorId(instructorId)
        .courseId(course.getId())
        .sectionId(section.getId())
        .build();
  }
}
