package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto convertToDto(Category entity);

    Category convertToEntity(NewCategoryDto dto);
}
