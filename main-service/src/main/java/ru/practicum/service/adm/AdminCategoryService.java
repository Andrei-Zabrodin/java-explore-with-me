package ru.practicum.service.adm;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long catId, NewCategoryDto dto);

    void deleteCategory(Long catId);
}
