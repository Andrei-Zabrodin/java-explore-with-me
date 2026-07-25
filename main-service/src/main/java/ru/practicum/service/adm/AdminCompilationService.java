package ru.practicum.service.adm;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;

public interface AdminCompilationService {

    CompilationDto createCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(UpdateCompilationRequest request, Long compId);

    void deleteCompilation(Long compId);
}
