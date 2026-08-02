package ru.practicum.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationEnrichmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationServiceImpl implements AdminCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final CompilationEnrichmentService compilationEnrichmentService;

    @Override
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = compilationMapper.convertToEntity(dto);

        List<Event> events = new ArrayList<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(dto.getEvents());

            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("One or more events were not found");
            }
        }
        compilation.setEvents(events);

        Compilation savedComp = compilationRepository.save(compilation);
        log.info("Created compilation with id: {}", savedComp.getId());

        return compilationEnrichmentService.enrichCompilationWithGivenEvents(savedComp, events);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest request, Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        List<Event> events = null;
        if (request.getEvents() != null) {
            events = eventRepository.findAllById(request.getEvents());
            if (events.size() != request.getEvents().size()) {
                throw new NotFoundException("One or more events were not found");
            }
            compilation.setEvents(events);
        }

        Optional.ofNullable(request.getTitle()).ifPresent(compilation::setTitle);
        Optional.ofNullable(request.getPinned()).ifPresent(compilation::setPinned);

        Compilation updatedComp = compilationRepository.save(compilation);
        log.info("Updated compilation with id: {}", updatedComp.getId());

        if (events == null)
            return compilationEnrichmentService.enrichCompilation(updatedComp);
        else {
            return compilationEnrichmentService.enrichCompilationWithGivenEvents(updatedComp, events);
        }
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Deleted compilation with id: {}", compId);
    }
}
