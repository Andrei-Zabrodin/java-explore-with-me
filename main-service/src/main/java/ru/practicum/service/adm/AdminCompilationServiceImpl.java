package ru.practicum.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationServiceImpl implements AdminCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

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

        return compilationMapper.convertToDto(savedComp);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest request, Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            if (events.size() != request.getEvents().size()) {
                throw new NotFoundException("One or more events were not found");
            }
            compilation.setEvents(events);
        }

        Compilation updatedComp = compilationRepository.save(compilation);
        log.info("Updated compilation with id: {}", updatedComp.getId());

        return compilationMapper.convertToDto(updatedComp);
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Deleted compilation with id: {}", compId);
    }
}
