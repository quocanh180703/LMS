package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.SectionController;
import com.example.nhom3_tt_.dtos.requests.section.SectionRequest;
import com.example.nhom3_tt_.dtos.response.lecture.LectureResponse;
import com.example.nhom3_tt_.dtos.response.section.SectionResponse;
import com.example.nhom3_tt_.mappers.SectionMapper;
import com.example.nhom3_tt_.models.Section;
import com.example.nhom3_tt_.services.LectureService;
import com.example.nhom3_tt_.services.SectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class SectionControllerTest {

  @Mock private SectionService sectionService;

  @Mock private SectionMapper sectionMapper;

  @Mock private LectureService lectureService;

  @InjectMocks private SectionController sectionController;

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(sectionController).build();
  }

  @Test
  void getAllSections_success() throws Exception {
    SectionResponse section1 =
        SectionResponse.builder()
            .id(1L)
            .name("Section 1")
            .description("Description 1")
            .position(1)
            .build();
    SectionResponse section2 =
        SectionResponse.builder()
            .id(2L)
            .name("Section 2")
            .description("Description 2")
            .position(2)
            .build();

    when(sectionService.getAll()).thenReturn(List.of(section1, section2));

    mockMvc
        .perform(get("/api/v1/sections"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Section 1"))
        .andExpect(jsonPath("$[1].name").value("Section 2"));
  }

  @Test
  void getSectionById_success() throws Exception {
    SectionResponse section =
        SectionResponse.builder()
            .id(1L)
            .name("Section 1")
            .description("Description 1")
            .position(1)
            .build();

    when(sectionService.getById(1L)).thenReturn(section);

    mockMvc
        .perform(get("/api/v1/sections/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Section 1"));
  }

  @Test
  void getLecturesBySectionId_success() throws Exception {
    // Setup response for lectures
    var lectureResponse =
        List.of(
            new LectureResponse(1L, "Lecture 1", "Description 1", true, "video1.mp4", "thumb1.png"),
            new LectureResponse(
                2L, "Lecture 2", "Description 2", false, "video2.mp4", "thumb2.png"));

    when(lectureService.getAllBySectionId(1L)).thenReturn(lectureResponse);

    mockMvc
        .perform(get("/api/v1/sections/1/lectures"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("Lecture 1"))
        .andExpect(jsonPath("$[1].title").value("Lecture 2"));
  }

  @Test
  void deleteSection_success() throws Exception {
    mockMvc.perform(delete("/api/v1/sections/1")).andExpect(status().isNoContent());
  }

  @Test
  void createSection_success() throws Exception {
    // Arrange
    SectionRequest sectionRequest = new SectionRequest();
    sectionRequest.setCourseId(1L);
    sectionRequest.setName("Section 1");
    sectionRequest.setDescription("Description 1");
    sectionRequest.setPosition(1);

    Section section = new Section();
    section.setId(1L);
    section.setName("Section 1");
    section.setDescription("Description 1");
    section.setPosition(1);

    SectionResponse sectionResponse = new SectionResponse();
    sectionResponse.setId(1L);
    sectionResponse.setName("Section 1");
    sectionResponse.setDescription("Description 1");
    sectionResponse.setCourseTitle("Course Title");
    sectionResponse.setPosition(1);

    // Mocking behavior of the service and mapper
    when(sectionMapper.toSection(any(SectionRequest.class))).thenReturn(section); // Mock toSection
    when(sectionService.create(any(Section.class))).thenReturn(sectionResponse);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/sections")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(sectionRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Section 1"))
        .andExpect(jsonPath("$.description").value("Description 1"))
        .andExpect(jsonPath("$.courseTitle").value("Course Title"))
        .andExpect(jsonPath("$.position").value(1));
  }

  @Test
  void updateSection_success() throws Exception {
    // Arrange
    SectionRequest sectionRequest = new SectionRequest();
    sectionRequest.setCourseId(1L);
    sectionRequest.setName("Updated Section");
    sectionRequest.setDescription("Updated Description");
    sectionRequest.setPosition(2);

    Section section = new Section();
    section.setId(1L);
    section.setName("Updated Section");
    section.setDescription("Updated Description");
    section.setPosition(2);

    SectionResponse sectionResponse = new SectionResponse();
    sectionResponse.setId(1L);
    sectionResponse.setName("Updated Section");
    sectionResponse.setDescription("Updated Description");
    sectionResponse.setCourseTitle("Updated Course Title");
    sectionResponse.setPosition(2);

    // Mocking behavior of the service and mapper
    when(sectionMapper.toSection(any(SectionRequest.class))).thenReturn(section); // Mock toSection
    when(sectionService.update(eq(1L), any(Section.class)))
        .thenReturn(sectionResponse); // Mock update

    // Act & Assert
    mockMvc
        .perform(
            put("/api/v1/sections/1")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(sectionRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Updated Section"))
        .andExpect(jsonPath("$.description").value("Updated Description"))
        .andExpect(jsonPath("$.courseTitle").value("Updated Course Title"))
        .andExpect(jsonPath("$.position").value(2));
  }
}
