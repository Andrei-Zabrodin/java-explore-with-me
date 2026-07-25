package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.model.Compilation;

@Mapper
public interface CompilationMapper {
    CompilationDto convertToDto(Compilation entity);

    Compilation convertToEntity(NewCompilationDto dto);
}
