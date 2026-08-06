package ru.practicum.controller.adm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.service.adm.AdminCategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCategoryController.class)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminCategoryService adminCategoryService;

    private NewCategoryDto newCategoryDto;
    private CategoryDto categoryDto;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        categoryId = 1L;

        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Концерты");

        categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Концерты");
    }

    // ============ POST /admin/categories ============

    @Test
    void createCategoryShouldReturnCreatedWhenValidRequest() throws Exception {
        when(adminCategoryService.createCategory(any(NewCategoryDto.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(adminCategoryService, times(1)).createCategory(any(NewCategoryDto.class));
    }

    @Test
    void createCategoryShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        newCategoryDto.setName("");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(adminCategoryService, never()).createCategory(any(NewCategoryDto.class));
    }

    @Test
    void createCategoryShouldReturnBadRequestWhenNameIsNull() throws Exception {
        newCategoryDto.setName(null);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(adminCategoryService, never()).createCategory(any(NewCategoryDto.class));
    }

    // ============ PATCH /admin/categories/{catId} ============

    @Test
    void updateCategoryShouldReturnOkWhenValidRequest() throws Exception {
        CategoryDto updatedDto = new CategoryDto();
        updatedDto.setId(categoryId);
        updatedDto.setName("Спектакли");

        when(adminCategoryService.updateCategory(eq(categoryId), any(NewCategoryDto.class)))
                .thenReturn(updatedDto);

        newCategoryDto.setName("Спектакли");

        mockMvc.perform(patch("/admin/categories/{catId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDto.getId()))
                .andExpect(jsonPath("$.name").value(updatedDto.getName()));

        verify(adminCategoryService, times(1))
                .updateCategory(eq(categoryId), any(NewCategoryDto.class));
    }

    @Test
    void updateCategoryShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        newCategoryDto.setName("");

        mockMvc.perform(patch("/admin/categories/{catId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(adminCategoryService, never())
                .updateCategory(eq(categoryId), any(NewCategoryDto.class));
    }

    @Test
    void updateCategoryShouldReturnBadRequestWhenNameIsTooLong() throws Exception {
        newCategoryDto.setName("A".repeat(60));  // > 50 символов

        mockMvc.perform(patch("/admin/categories/{catId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(adminCategoryService, never())
                .updateCategory(eq(categoryId), any(NewCategoryDto.class));
    }

    // ============ DELETE /admin/categories/{catId} ============

    @Test
    void deleteCategoryShouldReturnNoContentWhenCategoryExists() throws Exception {
        doNothing().when(adminCategoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/admin/categories/{catId}", categoryId))
                .andExpect(status().isNoContent());

        verify(adminCategoryService, times(1)).deleteCategory(categoryId);
    }

    @Test
    void deleteCategoryShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        Long nonExistentId = 999L;
        doThrow(NotFoundException.class).when(adminCategoryService).deleteCategory(nonExistentId);

        mockMvc.perform(delete("/admin/categories/{catId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(adminCategoryService, never()).deleteCategory(categoryId);
    }
}