package ru.practicum.service.adm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AdminCategoryServiceIntegrationTest {

    @Autowired
    private AdminCategoryService adminCategoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private NewCategoryDto newCategoryDto;
    private Category existingCategory;
    private User user;

    @BeforeEach
    void setUp() {
        newCategoryDto = new NewCategoryDto();
        newCategoryDto.setName("Концерты");

        existingCategory = new Category();
        existingCategory.setName("Существующая категория");
        existingCategory = categoryRepository.save(existingCategory);

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userRepository.save(user);
    }

    @Test
    void createCategoryShouldReturnCategoryDtoWhenValidRequest() {
        CategoryDto result = adminCategoryService.createCategory(newCategoryDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Концерты");

        Category savedCategory = categoryRepository.findById(result.getId()).orElseThrow();
        assertThat(savedCategory.getName()).isEqualTo("Концерты");
    }

    @Test
    void createCategoryShouldThrowConflictExceptionWhenNameAlreadyExists() {
        newCategoryDto.setName("Существующая категория");

        assertThatThrownBy(() -> adminCategoryService.createCategory(newCategoryDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Category with name 'Существующая категория' already exists");
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategoryDtoWhenValidRequest() {
        Long categoryId = existingCategory.getId();
        newCategoryDto.setName("Обновлённая категория");

        CategoryDto result = adminCategoryService.updateCategory(categoryId, newCategoryDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Обновлённая категория");

        Category updatedCategory = categoryRepository.findById(categoryId).orElseThrow();
        assertThat(updatedCategory.getName()).isEqualTo("Обновлённая категория");
    }

    @Test
    void updateCategoryShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminCategoryService.updateCategory(nonExistentId, newCategoryDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateCategoryShouldThrowConflictExceptionWhenNameAlreadyExists() {
        Long categoryId = existingCategory.getId();

        Category anotherCategory = new Category();
        anotherCategory.setName("Другая категория");
        categoryRepository.save(anotherCategory);

        newCategoryDto.setName("Другая категория");

        assertThatThrownBy(() -> adminCategoryService.updateCategory(categoryId, newCategoryDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Category with name '" + newCategoryDto.getName() + "' already exists");
    }

    @Test
    void updateCategoryShouldNotThrowConflictExceptionWhenUpdatingSameName() {
        Long categoryId = existingCategory.getId();
        newCategoryDto.setName("Существующая категория");

        CategoryDto result = adminCategoryService.updateCategory(categoryId, newCategoryDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Существующая категория");
    }

    @Test
    void deleteCategoryShouldDeleteWhenCategoryExistsAndNoEvents() {
        Long categoryId = existingCategory.getId();

        adminCategoryService.deleteCategory(categoryId);

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void deleteCategoryShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminCategoryService.deleteCategory(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }

    @Test
    void deleteCategoryShouldThrowConflictExceptionWhenCategoryHasEvents() {
        Long categoryId = existingCategory.getId();

        Event event = new Event();
        event.setTitle("Тестовое событие");
        event.setAnnotation("Тестовая аннотация");
        event.setDescription("Тестовое описание");
        event.setCategory(existingCategory);
        event.setInitiator(user);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLat(55.754167);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event.setPublishedOn(LocalDateTime.now());
        eventRepository.save(event);

        assertThatThrownBy(() -> adminCategoryService.deleteCategory(categoryId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("The category is not empty");
    }
}