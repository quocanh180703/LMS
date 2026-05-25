package com.example.nhom3_tt_.service;

import com.example.nhom3_tt_.dtos.requests.CategoryRequest;
import com.example.nhom3_tt_.dtos.response.CategoryResponse;
import com.example.nhom3_tt_.exception.NotFoundException;
import com.example.nhom3_tt_.mappers.CategoryMapper;
import com.example.nhom3_tt_.models.Category;
import com.example.nhom3_tt_.repositories.CategoryRepository;
import com.example.nhom3_tt_.services.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private CategoryMapper categoryMapper;

  @InjectMocks private CategoryServiceImpl categoryService;

  @Test
  void createCategory_success() {
    CategoryRequest request = new CategoryRequest();
    request.setName("Test Category");

    Category category = new Category();
    category.setName("Test Category");

    Category savedCategory = new Category();
    savedCategory.setId(1L);
    savedCategory.setName("Test Category");

    CategoryResponse response = new CategoryResponse();
    response.setId(1L);
    response.setName("Test Category");

    when(categoryMapper.convertToEntity(request)).thenReturn(category);
    when(categoryRepository.save(category)).thenReturn(savedCategory);
    when(categoryMapper.convertToResponse(savedCategory)).thenReturn(response);

    CategoryResponse result = categoryService.createCategory(request);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Category", result.getName());

    verify(categoryMapper).convertToEntity(request);
    verify(categoryRepository).save(category);
    verify(categoryMapper).convertToResponse(savedCategory);
  }

  @Test
  void getAllCategories_success() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Test Category");

    CategoryResponse response = new CategoryResponse();
    response.setId(1L);
    response.setName("Test Category");

    when(categoryRepository.findAll()).thenReturn(List.of(category));
    when(categoryMapper.convertToResponse(category)).thenReturn(response);

    List<CategoryResponse> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Category", result.get(0).getName());

    verify(categoryRepository).findAll();
    verify(categoryMapper).convertToResponse(category);
  }

  @Test
  void getCategoryById_success() {
    Long id = 1L;
    Category category = new Category();
    category.setId(id);
    category.setName("Test Category");
    category.setCourses(List.of());

    CategoryResponse response = new CategoryResponse();
    response.setId(id);
    response.setName("Test Category");

    when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
    when(categoryMapper.convertToResponse(category)).thenReturn(response);

    CategoryResponse result = categoryService.getCategoryById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals("Test Category", result.getName());

    verify(categoryRepository).findById(id);
    verify(categoryMapper).convertToResponse(category);
  }

  @Test
  void getCategoryById_notFound_throwsException() {
    Long id = 1L;

    when(categoryRepository.findById(id)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(id));

    //    assertEquals("Lấy danh sách không thành công với ID: " + id, exception.getMessage());
    verify(categoryRepository).findById(id);
  }

  @Test
  void updateCategory_success() {
    Long id = 1L;

    CategoryRequest request = new CategoryRequest();
    request.setName("Updated Category");

    Category existingCategory = new Category();
    existingCategory.setId(id);
    existingCategory.setName("Old Category");

    Category updatedCategory = new Category();
    updatedCategory.setId(id);
    updatedCategory.setName("Updated Category");

    CategoryResponse response = new CategoryResponse();
    response.setId(id);
    response.setName("Updated Category");

    when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(existingCategory)).thenReturn(updatedCategory);
    when(categoryMapper.convertToResponse(updatedCategory)).thenReturn(response);

    CategoryResponse result = categoryService.updateCategory(id, request);

    assertNotNull(result);
    assertEquals("Updated Category", result.getName());

    verify(categoryRepository).findById(id);
    verify(categoryRepository).save(existingCategory);
    verify(categoryMapper).convertToResponse(updatedCategory);
  }

  @Test
  void updateCategory_notFound_throwsException() {
    Long id = 1L;
    CategoryRequest request = new CategoryRequest();
    request.setName("Updated Category");

    when(categoryRepository.findById(id)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(id, request));

    assertEquals("Cập nhật không thành công với ID: " + id, exception.getMessage());
    verify(categoryRepository).findById(id);
  }

  @Test
  void deleteCategory_success() {
    Long id = 1L;

    Category category = new Category();
    category.setId(id);
    category.setName("Test Category");

    when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
    doNothing().when(categoryRepository).delete(category);

    assertDoesNotThrow(() -> categoryService.deleteCategory(id));

    verify(categoryRepository).findById(id);
    verify(categoryRepository).delete(category);
  }

  @Test
  void deleteCategory_notFound_throwsException() {
    Long id = 1L;

    when(categoryRepository.findById(id)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(id));

    assertEquals("Xóa không thành công với ID: " + id, exception.getMessage());
    verify(categoryRepository).findById(id);
  }
}
