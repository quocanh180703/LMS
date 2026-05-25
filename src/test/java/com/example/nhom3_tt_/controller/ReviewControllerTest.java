package com.example.nhom3_tt_.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.nhom3_tt_.controllers.ReviewController;
import com.example.nhom3_tt_.dtos.requests.ReviewRequest;
import com.example.nhom3_tt_.dtos.response.ReviewResponse;
import com.example.nhom3_tt_.services.ReviewService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

  @Mock private ReviewService reviewService;

  @InjectMocks private ReviewController reviewController;

  @Autowired private MockMvc mockMvc;

  @Test
  void getAllReviewsForCourse_success() throws Exception {
    mockMvc =
        MockMvcBuilders.standaloneSetup(reviewController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    ReviewResponse reviewResponse1 = new ReviewResponse();
    reviewResponse1.setId(1L);
    reviewResponse1.setCourseId(1L);

    ReviewResponse reviewResponse2 = new ReviewResponse();
    reviewResponse2.setId(2L);
    reviewResponse2.setCourseId(1L);

    when(reviewService.findAllReviewsByCourseId(eq(1L), any(Pageable.class)))
        .thenReturn(List.of(reviewResponse1, reviewResponse2));

    mockMvc
        .perform(get("/api/v1/courses/1/reviews").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].courseId").value(1L))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2L))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].courseId").value(1L));

    verify(reviewService).findAllReviewsByCourseId(eq(1L), any(Pageable.class));
  }

  @Test
  void addReviewToCourse_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    ReviewResponse reviewResponse = new ReviewResponse();
    reviewResponse.setId(1L);
    reviewResponse.setCourseId(1L);
    reviewResponse.setComment("good");
    reviewResponse.setStar(5.0);

    when(reviewService.addReviewToCourse(eq(1L), any(ReviewRequest.class)))
        .thenReturn(reviewResponse);

    mockMvc
        .perform(
            post("/api/v1/courses/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"good\",\"star\":5.0}"))
        .andExpect(status().isOk());

    verify(reviewService).addReviewToCourse(eq(1L), any(ReviewRequest.class));
  }

  @Test
  void getReviewById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    ReviewResponse reviewResponse = new ReviewResponse();

    when(reviewService.findReviewById(1L)).thenReturn(reviewResponse);

    mockMvc.perform(get("/api/v1/reviews/1")).andExpect(status().isOk());

    verify(reviewService).findReviewById(1L);
  }

  @Test
  void updateReviewById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    ReviewResponse reviewResponse = new ReviewResponse();
    reviewResponse.setId(1L);
    reviewResponse.setCourseId(1L);
    reviewResponse.setComment("good");
    reviewResponse.setStar(5.0);

    when(reviewService.updateReviewById(eq(1L), any(ReviewRequest.class)))
        .thenReturn(reviewResponse);

    mockMvc
        .perform(
            put("/api/v1/courses/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"good\",\"star\":5.0}"))
        .andExpect(status().isOk());
  }

  @Test
  void deleteReviewById_success() throws Exception {
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    doNothing().when(reviewService).deleteReviewById(1L);

    mockMvc.perform(delete("/api/v1/courses/reviews/1")).andExpect(status().isNoContent());

    verify(reviewService).deleteReviewById(1L);
  }
}
