package ru.practicum.service.adm;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

public interface AdminCompilationService {

    CompilationDto createCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(UpdateCompilationRequest request, Long compId);

    void deleteCompilation(Long compId);
}
