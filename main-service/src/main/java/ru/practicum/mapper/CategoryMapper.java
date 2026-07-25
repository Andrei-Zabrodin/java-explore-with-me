package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.model.Category;

@Mapper
public interface CategoryMapper {
    CategoryDto convertToDto(Category entity);

    Category convertToEntity(NewCategoryDto dto);
}
