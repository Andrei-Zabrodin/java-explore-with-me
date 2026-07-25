package ru.practicum.controller.adm;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.service.adm.AdminCompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {
    private final AdminCompilationService adminCompilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto dto) {
        log.info("POST /admin/compilations - создание подборки с названием: {}", dto.getTitle());
        return adminCompilationService.createCompilation(dto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto patchCompilation(@Valid @RequestBody UpdateCompilationRequest request,
                                           @PathVariable Long compId) {
        log.info("PATCH /admin/compilations/{} - обновление подборки", compId);
        return adminCompilationService.updateCompilation(request, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{} - удаление подборки", compId);
        adminCompilationService.deleteCompilation(compId);
    }
}
