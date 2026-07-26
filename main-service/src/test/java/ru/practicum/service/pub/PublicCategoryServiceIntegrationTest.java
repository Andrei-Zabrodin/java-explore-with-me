package ru.practicum.service.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PublicCategoryServiceIntegrationTest {

    @Autowired
    private PublicCategoryService publicCategoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category existingCategory1;
    private Category existingCategory2;

    @BeforeEach
    void setUp() {
        existingCategory1 = new Category();
        existingCategory1.setName("Category 1");
        existingCategory1 = categoryRepository.save(existingCategory1);

        existingCategory2 = new Category();
        existingCategory2.setName("Category 2");
        existingCategory2 = categoryRepository.save(existingCategory2);
    }

    @Test
    void getCategoriesShouldReturnListWithDefaultPagination() {
        List<CategoryDto> result = publicCategoryService.getCategories(0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryDto::getName)
                .containsExactlyInAnyOrder(existingCategory1.getName(), existingCategory2.getName());
    }

    @Test
    void getCategoriesShouldReturnLimitedListWithPagination() {
        List<CategoryDto> result = publicCategoryService.getCategories(0, 1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getCategoriesShouldReturnEmptyListWhenPageOutOfRange() {
        List<CategoryDto> result = publicCategoryService.getCategories(10, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryByIdShouldReturnCategoryWhenExists() {
        CategoryDto result = publicCategoryService.getCategoryById(existingCategory1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingCategory1.getId());
        assertThat(result.getName()).isEqualTo(existingCategory1.getName());
    }

    @Test
    void getCategoryByIdShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> publicCategoryService.getCategoryById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }
}