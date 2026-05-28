package com.example.nhom3_tt_.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.nhom3_tt_.dtos.requests.lecture.LectureVideoFolderStructure;
import com.example.nhom3_tt_.exception.AppException;
import com.example.nhom3_tt_.services.impl.CloudinaryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class CloudinaryServiceTest {

  @Spy @InjectMocks private CloudinaryServiceImpl cloudinaryService;

  @Mock private MultipartFile mockFile;

  @Mock private Cloudinary cloudinary;

  @Mock private Uploader uploader;

  @Value("${cloudinary.application.name}")
  private String applicationName = "testApp";

  private LectureVideoFolderStructure lectureVideoFolderStructure;

  private final String APPLICATION_NAME = "test-app";
  private final String EXPECTED_VIDEO_URL = "https://cloudinary.com/test-video.mp4";
  private final String EXPECTED_IMAGE_URL = "https://cloudinary.com/test-image.jpg";

  private File testFile;

  @BeforeEach
  void initData() throws IOException {
    lectureVideoFolderStructure = new LectureVideoFolderStructure(1L, 2L, 3L);
    ReflectionTestUtils.setField(cloudinaryService, "APPLICATION_NAME", APPLICATION_NAME);
  }

  @AfterEach
  void cleanUp() {
    if (testFile != null && testFile.exists()) {
      testFile.delete();
    }
  }

  @Test
  void convert_validFile_success() throws IOException {
    // Arrange
    String originalFilename = "test.txt";
    byte[] content = "Body".getBytes();
    when(mockFile.getOriginalFilename()).thenReturn(originalFilename);
    when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

    // Act
    testFile = cloudinaryService.convert(mockFile);

    // Assert
    assertNotNull(testFile);
    assertTrue(testFile.exists());
    assertEquals(content.length, testFile.length());
    assertArrayEquals(content, Files.readAllBytes(testFile.toPath()));
  }

  @Test
  void convert_nullFile_throwsAppException() {
    AppException exception = assertThrows(AppException.class, () -> cloudinaryService.convert(null));

    assertEquals("File is empty", exception.getMessage());
  }

  @Test
  void uploadVideo_withoutFolder_shouldReturnUrl() throws IOException {
    MockMultipartFile file =
        new MockMultipartFile("video", "demo-video.mp4", "video/mp4", "demo".getBytes());

    Map<String, String> uploadResult = new HashMap<>();
    uploadResult.put("url", EXPECTED_VIDEO_URL);

    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.upload(any(File.class), any(Map.class))).thenReturn(uploadResult);

    String result = cloudinaryService.uploadVideo(file);

    assertEquals(EXPECTED_VIDEO_URL, result);
    verify(cloudinary).uploader();
    verify(uploader).upload(any(File.class), any(Map.class));
  }

  @Test
  void uploadImage_withoutFolder_shouldReturnUrl() throws IOException {
    MockMultipartFile file =
        new MockMultipartFile("image", "demo-image.jpg", "image/jpeg", "demo".getBytes());

    Map<String, String> uploadResult = new HashMap<>();
    uploadResult.put("url", EXPECTED_IMAGE_URL);

    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.upload(any(File.class), any(Map.class))).thenReturn(uploadResult);

    String result = cloudinaryService.uploadImage(file);

    assertEquals(EXPECTED_IMAGE_URL, result);
    verify(cloudinary).uploader();
    verify(uploader).upload(any(File.class), any(Map.class));
  }

  @Test
  void getFileName_shouldSplitOnDots() {
    String[] parts = cloudinaryService.getFileName("lesson.final.mp4");

    assertArrayEquals(new String[] {"lesson", "final", "mp4"}, parts);
  }

  @Test
  void generatePublicValue_shouldAppendOriginalStem() {
    String publicValue = cloudinaryService.generatePublicValue("lesson.final.mp4");

    assertTrue(publicValue.endsWith("_lesson"));
    assertTrue(publicValue.contains("_"));
  }

  @Test
  void cleanDisk_shouldSwallowIOExceptionWhenFileDoesNotExist() {
    cloudinaryService.cleanDisk(new File("non-existent-file-for-clean-disk-test.tmp"));
  }

  @Test
  public void uploadVideo_shouldReturnUrl_WhenUploadIsSuccessful() throws IOException {
    // Arrange
    MockMultipartFile mockFile =
        new MockMultipartFile(
            "video", "test-video.mp4", "video/mp4", "test video content".getBytes());

    LectureVideoFolderStructure folderStructure =
        LectureVideoFolderStructure.builder().instructorId(1L).courseId(2L).sectionId(3L).build();

    Map<String, Object> expectedParams = new HashMap<>();
    String expectedFolder = String.join("/", APPLICATION_NAME, "1", "2", "3");
    expectedParams.put("resource_type", "video");
    expectedParams.put("folder", expectedFolder);
    expectedParams.put(
        "public_id", cloudinaryService.generatePublicValue(mockFile.getOriginalFilename()));

    Map<String, String> uploadResult = new HashMap<>();
    uploadResult.put("url", EXPECTED_IMAGE_URL);

    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.upload(any(File.class), any(Map.class))).thenReturn(uploadResult);

    // Act
    String resultUrl = cloudinaryService.uploadVideo(mockFile, folderStructure);

    // Assert
    assertNotNull(resultUrl);
    assertEquals(EXPECTED_IMAGE_URL, resultUrl);
    verify(cloudinary, times(1)).uploader();
    verify(uploader, times(1)).upload(any(File.class), any(Map.class));
  }

  @Test
  void uploadImage_ShouldReturnUrl_WhenUploadIsSuccessful() throws IOException {
    // Arrange
    MockMultipartFile mockFile =
        new MockMultipartFile(
            "image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

    LectureVideoFolderStructure folderStructure =
        LectureVideoFolderStructure.builder().instructorId(1L).courseId(2L).sectionId(3L).build();

    Map<String, Object> expectedParams = new HashMap<>();
    String expectedFolder = String.join("/", APPLICATION_NAME, "1", "2", "3");
    expectedParams.put("folder", expectedFolder);
    expectedParams.put(
        "public_id", cloudinaryService.generatePublicValue(mockFile.getOriginalFilename()));

    Map<String, String> uploadResult = new HashMap<>();
    uploadResult.put("url", EXPECTED_IMAGE_URL);

    when(cloudinary.uploader()).thenReturn(uploader);
    when(uploader.upload(any(File.class), any(Map.class))).thenReturn(uploadResult);

    // Act
    String resultUrl = cloudinaryService.uploadImage(mockFile, folderStructure);

    // Assert
    assertNotNull(resultUrl);
    assertEquals(EXPECTED_IMAGE_URL, resultUrl);
    verify(cloudinary, times(1)).uploader();
    verify(uploader, times(1)).upload(any(File.class), any(Map.class));
  }
}
