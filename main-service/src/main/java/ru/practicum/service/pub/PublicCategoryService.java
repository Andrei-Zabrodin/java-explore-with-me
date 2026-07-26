package ru.practicum.service.pub;

import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface PublicCategoryService {
    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);
}
