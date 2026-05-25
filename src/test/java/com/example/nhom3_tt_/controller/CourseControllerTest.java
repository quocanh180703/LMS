package com.example.nhom3_tt_.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.nhom3_tt_.controllers.CourseController;
import com.example.nhom3_tt_.dtos.requests.CourseRequest;
import com.example.nhom3_tt_.dtos.response.CourseResponse;
import com.example.nhom3_tt_.dtos.response.section.SectionResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.services.CourseService;
import com.example.nhom3_tt_.services.SectionService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerTest {

  private MockMvc mockMvc;

  @Mock private CourseService courseService;

  @Mock private SectionService sectionService;

  @InjectMocks private CourseController courseController;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
  }

  @Test
  public void testCreateCourse_InvalidRequest_TitleEmpty() throws Exception {
    // Giả lập yêu cầu với tiêu đề trống
    CourseRequest courseRequest =
        CourseRequest.builder()
            .instructorId(1L)
            .categoryId(1L)
            .title("") // Tiêu đề trống
            .shortDescription("Valid short description")
            .level("Beginner")
            .regularPrice(25000)
            .publishDay(new Date())
            .status("Active")
            .language("English")
            .thumbnail("thumbnail.jpg")
            .requireLogin(true)
            .introVideo("intro.mp4")
            .requirement("Valid requirement")
            .objective("Valid objective")
            .description("Valid description")
            .closeCaption("Valid close caption")
            .build();

    // Thực hiện kiểm tra
    mockMvc
        .perform(
            post("/api/v1/courses")
                .contentType("application/json")
                .content(
                    "{\"instructorId\": 1, \"categoryId\": 1, \"title\": \"\", "
                        + "\"shortDescription\": \"Valid short description\", \"level\": \"Beginner\", "
                        + "\"regularPrice\": 25000, \"publishDay\": \"2025-01-03\", \"status\": \"Active\", "
                        + "\"language\": \"English\", \"thumbnail\": \"thumbnail.jpg\", \"requireLogin\": true, "
                        + "\"introVideo\": \"intro.mp4\", \"requirement\": \"Valid requirement\", "
                        + "\"objective\": \"Valid objective\", \"description\": \"Valid description\", "
                        + "\"closeCaption\": \"Valid close caption\"}"))
        .andExpect(status().isBadRequest()); // Mong đợi trả về lỗi 400 (Bad Request)
  }

  @Test
  public void testForceDeleteCourse() throws Exception {
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Deleted Course");

    when(courseService.forceDelete(1L)).thenReturn(courseResponse);

    mockMvc
        .perform(delete("/api/v1/courses/force-delete/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Deleted Course"));
  }

  @Test
  public void testApproveCourse() throws Exception {
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Approved Course");

    when(courseService.approveCourse(1L)).thenReturn(courseResponse);

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/approve", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Approved Course"));
  }

  @Test
  public void testRejectCourse() throws Exception {
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Rejected Course");

    when(courseService.rejectCourse(1L)).thenReturn(courseResponse);

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/reject", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Rejected Course"));
  }

  @Test
  public void testGetCourseById() throws Exception {
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Course 1");

    when(courseService.getById(1L)).thenReturn(courseResponse);

    mockMvc
        .perform(get("/api/v1/courses/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Course 1"));
  }

  @Test
  public void testApproveCourse_IllegalStateException() throws Exception {
    // Giả lập lỗi IllegalStateException khi approve
    when(courseService.approveCourse(1L)).thenThrow(new IllegalStateException("Invalid state"));

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/approve", 1L))
        .andExpect(status().isBadRequest()); // Mong đợi trả về lỗi 400 (Bad Request)
  }

  @Test
  public void testApproveCourse_NotFoundException() throws Exception {
    // Giả lập lỗi NotFoundException khi approve
    when(courseService.approveCourse(1L)).thenThrow(new NotFoundException("Course not found"));

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/approve", 1L))
        .andExpect(status().isNotFound()); // Mong đợi trả về lỗi 404 (Not Found)
  }

  @Test
  public void testRejectCourse_IllegalStateException() throws Exception {
    // Giả lập lỗi IllegalStateException khi reject
    when(courseService.rejectCourse(1L)).thenThrow(new IllegalStateException("Invalid state"));

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/reject", 1L))
        .andExpect(status().isBadRequest()); // Mong đợi trả về lỗi 400 (Bad Request)
  }

  @Test
  public void testRejectCourse_NotFoundException() throws Exception {
    // Giả lập lỗi NotFoundException khi reject
    when(courseService.rejectCourse(1L)).thenThrow(new NotFoundException("Course not found"));

    mockMvc
        .perform(put("/api/v1/courses/{courseID}/reject", 1L))
        .andExpect(status().isNotFound()); // Mong đợi trả về lỗi 404 (Not Found)
  }

  @Test
  public void testCreateCourse_ValidRequest() throws Exception {
    // Mock CourseRequest
    CourseRequest courseRequest =
        CourseRequest.builder()
            .instructorId(1L)
            .categoryId(1L)
            .title("Valid Course Title")
            .shortDescription("Valid short description")
            .level("Beginner")
            .regularPrice(25000)
            .publishDay(new Date())
            .status("Active")
            .language("English")
            .thumbnail("thumbnail.jpg")
            .requireLogin(true)
            .introVideo("intro.mp4")
            .requirement("Valid requirement")
            .objective("Valid objective")
            .description("Valid description")
            .closeCaption("Valid close caption")
            .build();

    // Mock CourseResponse
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L); // Giả lập id được tạo tự động
    courseResponse.setTitle("Valid Course Title");

    // Cấu hình mock để trả về CourseResponse
    when(courseService.create(any(CourseRequest.class))).thenReturn(courseResponse);

    // Thực hiện kiểm tra
    mockMvc
        .perform(
            post("/api/v1/courses")
                .contentType("application/json")
                .content(
                    "{\"instructorId\": 1, \"categoryId\": 1, \"title\": \"Valid Course Title\", "
                        + "\"shortDescription\": \"Valid short description\", \"level\": \"Beginner\", "
                        + "\"regularPrice\": 25000, \"publishDay\": \"2025-01-03\", \"status\": \"Active\", "
                        + "\"language\": \"English\", \"thumbnail\": \"thumbnail.jpg\", \"requireLogin\": true, "
                        + "\"introVideo\": \"intro.mp4\", \"requirement\": \"Valid requirement\", "
                        + "\"objective\": \"Valid objective\", \"description\": \"Valid description\", "
                        + "\"closeCaption\": \"Valid close caption\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1)) // Mong đợi id trong phản hồi
        .andExpect(jsonPath("$.title").value("Valid Course Title"));
  }

  @Test
  public void testUpdateCourse_ValidRequest() throws Exception {
    // Mock CourseRequest (cập nhật thông tin khóa học)
    CourseRequest courseRequest =
        CourseRequest.builder()
            .instructorId(1L)
            .categoryId(1L)
            .title("Updated Course Title")
            .shortDescription("Updated short description")
            .level("Advanced")
            .regularPrice(30000)
            .publishDay(new Date())
            .status("Active")
            .language("English")
            .thumbnail("updated_thumbnail.jpg")
            .requireLogin(true)
            .introVideo("updated_intro.mp4")
            .requirement("Updated requirement")
            .objective("Updated objective")
            .description("Updated description")
            .closeCaption("Updated close caption")
            .build();

    // Mock CourseResponse (phản hồi sau khi cập nhật)
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L); // ID của khóa học đã được cập nhật
    courseResponse.setTitle("Updated Course Title");

    // Cấu hình mock để trả về CourseResponse
    when(courseService.update(eq(1L), any(CourseRequest.class))).thenReturn(courseResponse);

    // Thực hiện kiểm tra
    mockMvc
        .perform(
            put("/api/v1/courses/{id}", 1L)
                .contentType("application/json")
                .content(
                    "{\"instructorId\": 1, \"categoryId\": 1, \"title\": \"Updated Course Title\", "
                        + "\"shortDescription\": \"Updated short description\", \"level\": \"Advanced\", "
                        + "\"regularPrice\": 30000, \"publishDay\": \"2025-01-03\", \"status\": \"Active\", "
                        + "\"language\": \"English\", \"thumbnail\": \"updated_thumbnail.jpg\", \"requireLogin\": true, "
                        + "\"introVideo\": \"updated_intro.mp4\", \"requirement\": \"Updated requirement\", "
                        + "\"objective\": \"Updated objective\", \"description\": \"Updated description\", "
                        + "\"closeCaption\": \"Updated close caption\"}"))
        .andExpect(status().isOk()) // Mong đợi trạng thái 200 OK
        .andExpect(jsonPath("$.id").value(1)) // Kiểm tra ID trong phản hồi
        .andExpect(
            jsonPath("$.title")
                .value("Updated Course Title")); // Kiểm tra tên khóa học trong phản hồi
  }

  @Test
  void getAllCourses_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getAll
    when(courseService.getAll(any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang
    mockMvc
        .perform(
            get("/api/v1/courses")
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Course 2"));

    // Kiểm tra xem phương thức getAll trong service có được gọi đúng số lần và với Pageable
    verify(courseService, times(1)).getAll(any(Pageable.class));
  }

  @Test
  void searchByTitle_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học với title chứa từ "Course"
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức searchByTitle
    when(courseService.searchByTitle(eq("Course"), any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang và title
    mockMvc
        .perform(
            get("/api/v1/courses/title")
                .param("title", "Course") // Tham số tìm kiếm theo title
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Course 2"));

    // Kiểm tra xem phương thức searchByTitle trong service có được gọi đúng số lần và với title và
    // Pageable
    verify(courseService, times(1)).searchByTitle(eq("Course"), any(Pageable.class));
  }

  @Test
  void getByLevel_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học có level "Beginner"
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Beginner Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Beginner Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getCoursesByLevel
    when(courseService.getCoursesByLevel(eq("Beginner"), any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang và level
    mockMvc
        .perform(
            get("/api/v1/courses/level/{level}", "Beginner")
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Beginner Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Beginner Course 2"));

    // Kiểm tra xem phương thức getCoursesByLevel trong service có được gọi đúng số lần và với level
    // và Pageable
    verify(courseService, times(1)).getCoursesByLevel(eq("Beginner"), any(Pageable.class));
  }

  @Test
  void getByCategoryId_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học trong categoryId = 1
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Category 1 Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Category 1 Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getCoursesByCategoryId
    when(courseService.getCoursesByCategoryId(eq(1L), any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang và categoryId
    mockMvc
        .perform(
            get("/api/v1/courses/category/{id}", 1L)
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Category 1 Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Category 1 Course 2"));

    // Kiểm tra xem phương thức getCoursesByCategoryId trong service có được gọi đúng số lần và với
    // categoryId và Pageable
    verify(courseService, times(1)).getCoursesByCategoryId(eq(1L), any(Pageable.class));
  }

  @Test
  void getByInstructorId_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học với instructorId = 1
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Instructor 1 Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Instructor 1 Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getCourseByInstructorId
    when(courseService.getCourseByIntructorId(eq(1L), any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang và instructorId
    mockMvc
        .perform(
            get("/api/v1/courses/instructor/{id}", 1L)
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Instructor 1 Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Instructor 1 Course 2"));

    // Kiểm tra xem phương thức getCourseByInstructorId trong service có được gọi đúng số lần và với
    // instructorId và Pageable
    verify(courseService, times(1)).getCourseByIntructorId(eq(1L), any(Pageable.class));
  }

  @Test
  void getAllApproved_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học đã được duyệt
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Approved Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Approved Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getAllApproved
    when(courseService.getAllApproved(any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang
    mockMvc
        .perform(
            get("/api/v1/courses/get-approveds")
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Approved Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Approved Course 2"));

    // Kiểm tra xem phương thức getAllApproved trong service có được gọi đúng số lần và với Pageable
    verify(courseService, times(1)).getAllApproved(any(Pageable.class));
  }

  @Test
  void getAllReject_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học bị từ chối
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Rejected Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Rejected Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getAllReject
    when(courseService.getAllReject(any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang
    mockMvc
        .perform(
            get("/api/v1/courses/get-rejects")
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Rejected Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Rejected Course 2"));

    // Kiểm tra xem phương thức getAllReject trong service có được gọi đúng số lần và với Pageable
    verify(courseService, times(1)).getAllReject(any(Pageable.class));
  }

  @Test
  void getAllPending_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các khóa học đang chờ phê duyệt
    CourseResponse courseResponse1 = new CourseResponse();
    courseResponse1.setId(1L);
    courseResponse1.setTitle("Pending Course 1");

    CourseResponse courseResponse2 = new CourseResponse();
    courseResponse2.setId(2L);
    courseResponse2.setTitle("Pending Course 2");

    // Mô phỏng hành vi của service khi gọi phương thức getAllPending
    when(courseService.getAllPending(any(Pageable.class)))
        .thenReturn(List.of(courseResponse1, courseResponse2));

    // Thực hiện yêu cầu GET với tham số phân trang
    mockMvc
        .perform(
            get("/api/v1/courses/get-pendings")
                .param("page", "0") // Trang đầu tiên
                .param("size", "10")) // Số lượng item mỗi trang là 10
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng khóa học trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra khóa học đầu tiên
        .andExpect(jsonPath("$[0].title").value("Pending Course 1"))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra khóa học thứ hai
        .andExpect(jsonPath("$[1].title").value("Pending Course 2"));

    // Kiểm tra xem phương thức getAllPending trong service có được gọi đúng số lần và với Pageable
    verify(courseService, times(1)).getAllPending(any(Pageable.class));
  }

  @Test
  void getSectionsByCourseId_success() throws Exception {
    // Khởi tạo mockMvc với các argument resolver cho Pageable
    mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    // Tạo dữ liệu giả cho các SectionResponse
    SectionResponse sectionResponse1 =
        SectionResponse.builder()
            .id(1L)
            .name("Section 1")
            .description("Description for section 1")
            .courseTitle("Course 1")
            .position(1)
            .build();

    SectionResponse sectionResponse2 =
        SectionResponse.builder()
            .id(2L)
            .name("Section 2")
            .description("Description for section 2")
            .courseTitle("Course 1")
            .position(2)
            .build();

    // Mô phỏng hành vi của service khi gọi phương thức getAllByCourseId
    when(sectionService.getAllByCourseId(eq(1L)))
        .thenReturn(List.of(sectionResponse1, sectionResponse2));

    // Thực hiện yêu cầu GET với tham số courseId
    mockMvc
        .perform(get("/api/v1/courses/{id}/sections", 1L)) // Gửi yêu cầu GET với id khóa học
        .andExpect(status().isOk()) // Kiểm tra mã trạng thái trả về là 200 OK
        .andExpect(jsonPath("$", hasSize(2))) // Kiểm tra số lượng section trong kết quả
        .andExpect(jsonPath("$[0].id").value(1L)) // Kiểm tra section đầu tiên
        .andExpect(jsonPath("$[0].name").value("Section 1"))
        .andExpect(jsonPath("$[0].description").value("Description for section 1"))
        .andExpect(jsonPath("$[0].courseTitle").value("Course 1"))
        .andExpect(jsonPath("$[0].position").value(1))
        .andExpect(jsonPath("$[1].id").value(2L)) // Kiểm tra section thứ hai
        .andExpect(jsonPath("$[1].name").value("Section 2"))
        .andExpect(jsonPath("$[1].description").value("Description for section 2"))
        .andExpect(jsonPath("$[1].courseTitle").value("Course 1"))
        .andExpect(jsonPath("$[1].position").value(2));

    // Kiểm tra xem phương thức getAllByCourseId trong service có được gọi đúng số lần và với
    // courseId là 1
    verify(sectionService, times(1)).getAllByCourseId(eq(1L));
  }

  @Test
  public void testUploadThumbnail() throws Exception {
    // Tạo đối tượng CourseResponse giả lập
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Course with Thumbnail");

    // Mô phỏng hành vi của service
    when(courseService.uploadThumbnail(eq(1L), any(MultipartFile.class)))
        .thenReturn(courseResponse);

    // Tạo tệp giả lập
    MockMultipartFile file =
        new MockMultipartFile("file", "thumbnail.jpg", "image/jpeg", "content".getBytes());

    // Thực hiện yêu cầu upload thumbnail
    mockMvc
        .perform(
            multipart("/api/v1/courses/{id}/upload-thumbnail", 1L)
                .file(file)
                .header("Authorization", "Bearer token")) // Thêm header Authorization nếu cần
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Course with Thumbnail"));
  }

  @Test
  public void testUploadIntroVideo() throws Exception {
    // Tạo đối tượng CourseResponse giả lập
    CourseResponse courseResponse = new CourseResponse();
    courseResponse.setId(1L);
    courseResponse.setTitle("Course with Intro Video");

    // Mô phỏng hành vi của service
    when(courseService.uploadVideo(eq(1L), any(MultipartFile.class))).thenReturn(courseResponse);

    // Tạo tệp video giả lập
    MockMultipartFile file =
        new MockMultipartFile("file", "intro-video.mp4", "video/mp4", "video content".getBytes());

    // Thực hiện yêu cầu upload video intro
    mockMvc
        .perform(
            multipart("/api/v1/courses/{id}/upload-intro-video", 1L)
                .file(file)
                .header("Authorization", "Bearer token")) // Thêm header Authorization nếu cần
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Course with Intro Video"));
  }
}
