package com.example.nhom3_tt_.controller;

import com.example.nhom3_tt_.controllers.CategoryController;
import com.example.nhom3_tt_.dtos.requests.CategoryRequest;
import com.example.nhom3_tt_.dtos.response.CategoryResponse;
import com.example.nhom3_tt_.services.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() throws Exception {
        List<CategoryResponse> categories = Arrays.asList(
                new CategoryResponse(1L, "Category 1"),
                new CategoryResponse(2L, "Category 2")
        );

        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Category 1"));
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        CategoryResponse categoryResponse = new CategoryResponse(1L, "Category 1");

        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Category 1"));
    }

    @Test
    void createCategory_shouldCreateAndReturnCategory() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest("New Category");
        CategoryResponse categoryResponse = new CategoryResponse(1L, "New Category");

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void updateCategory_shouldUpdateAndReturnCategory() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest("Updated Category");
        CategoryResponse categoryResponse = new CategoryResponse(1L, "Updated Category");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void deleteCategory_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete category successfully"));
    }
}
