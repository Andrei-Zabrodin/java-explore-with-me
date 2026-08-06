package ru.practicum.service.adm;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long catId, NewCategoryDto dto);

    void deleteCategory(Long catId);
}
