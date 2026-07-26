package ru.practicum.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.pub.PublicCategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicCategoryController.class)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicCategoryService publicCategoryService;

    private Long categoryId;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        categoryId = 1L;

        categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Концерты");
    }

    // ============ GET /categories ============

    @Test
    void getCategoriesShouldReturnListWhenValidRequest() throws Exception {
        List<CategoryDto> categories = List.of(categoryDto);

        when(publicCategoryService.getCategories(anyInt(), anyInt()))
                .thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryDto.getId()))
                .andExpect(jsonPath("$[0].name").value(categoryDto.getName()));

        verify(publicCategoryService, times(1))
                .getCategories(anyInt(), anyInt());
    }

    @Test
    void getCategoriesShouldReturnOkWithDefaultParameters() throws Exception {
        List<CategoryDto> categories = List.of(categoryDto);

        when(publicCategoryService.getCategories(eq(0), eq(10)))
                .thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

        verify(publicCategoryService, times(1))
                .getCategories(eq(0), eq(10));
    }

    @Test
    void getCategoriesShouldReturnBadRequestWhenInvalidFrom() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCategoryService, never())
                .getCategories(anyInt(), anyInt());
    }

    @Test
    void getCategoriesShouldReturnBadRequestWhenInvalidSize() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("size", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCategoryService, never())
                .getCategories(anyInt(), anyInt());
    }

    @Test
    void getCategoriesShouldReturnBadRequestWhenNegativeFrom() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(publicCategoryService, never())
                .getCategories(anyInt(), anyInt());
    }

    @Test
    void getCategoriesShouldReturnBadRequestWhenZeroSize() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(publicCategoryService, never())
                .getCategories(anyInt(), anyInt());
    }

    // ============ GET /categories/{catId} ============

    @Test
    void getCategoryByIdShouldReturnCategoryWhenValidRequest() throws Exception {
        when(publicCategoryService.getCategoryById(eq(categoryId)))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/categories/{catId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(publicCategoryService, times(1))
                .getCategoryById(eq(categoryId));
    }

    @Test
    void getCategoryByIdShouldReturnBadRequestWhenInvalidCategoryId() throws Exception {
        mockMvc.perform(get("/categories/{catId}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCategoryService, never())
                .getCategoryById(anyLong());
    }
}