package com.example.nhom3_tt_.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.LectureController;
import com.example.nhom3_tt_.dtos.requests.lecture.LectureRequest;
import com.example.nhom3_tt_.dtos.requests.lecture.LectureVideoFolderStructure;
import com.example.nhom3_tt_.dtos.response.lecture.LectureResponse;
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
import com.example.nhom3_tt_.services.impl.LectureServiceImpl;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class LectureControllerTest {

    @Mock
    private LectureService lectureService;

    @InjectMocks
    private LectureController lectureController;

    @Autowired
    private MockMvc mockMvc;
    @Mock private LectureMapper mapper;
    @Mock private SectionService sectionService;
    @Mock private CourseService courseService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private LectureRepository repository;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getLecture_success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(lectureController).build();

        LectureResponse response = LectureResponse.builder()
                .id(1L)
                .title("Sample Lecture")
                .description("Description of lecture")
                .preview(true)
                .linkVideo("video_url")
                .thumbnail("thumbnail_url")
                .build();

        when(lectureService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/lecture/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Sample Lecture"))
                .andExpect(jsonPath("$.description").value("Description of lecture"))
                .andExpect(jsonPath("$.preview").value(true))
                .andExpect(jsonPath("$.linkVideo").value("video_url"))
                .andExpect(jsonPath("$.thumbnail").value("thumbnail_url"));

        verify(lectureService, times(1)).getById(1L);
    }

    @Test
    void getAllLectures_success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(lectureController).build();

        mockMvc.perform(get("/api/v1/lecture"))
                .andExpect(status().isOk());

        verify(lectureService, times(1)).getAll();
    }

    @Test
    void deleteLecture_success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(lectureController).build();

        mockMvc.perform(delete("/api/v1/lecture/1"))
                .andExpect(status().isOk());

        verify(lectureService, times(1)).delete(1L);
    }

}
