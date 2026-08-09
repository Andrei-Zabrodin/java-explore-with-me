package ru.practicum.service.adm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.BaseCompilationService;
import ru.practicum.service.ViewsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AdminCompilationServiceImpl extends BaseCompilationService implements AdminCompilationService {
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final ViewsService viewsService;

    public AdminCompilationServiceImpl(CompilationRepository compilationRepository,
                                       EventRepository eventRepository,
                                       CompilationMapper compilationMapper,
                                       EventMapper eventMapper,
                                       ViewsService viewsService) {
        super(compilationRepository);
        this.eventRepository = eventRepository;
        this.compilationMapper = compilationMapper;
        this.eventMapper = eventMapper;
        this.viewsService = viewsService;
    }

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

        // Запрашиваем просмотры событий и создаём дто для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(events);
        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events, viewsMap);

        return compilationMapper.convertToDto(savedComp, eventDtos);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest request, Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        List<Event> events;
        if (request.getEvents() != null) {
            events = eventRepository.findAllById(request.getEvents());
            if (events.size() != request.getEvents().size()) {
                throw new NotFoundException("One or more events were not found");
            }
            compilation.setEvents(events);
        } else {
            // Получаем список id событий и ищем соответствующие события
            List<Long> eventIds = getCompilationEventsList(compilation);
            events = eventRepository.findAllByIdWithFetch(eventIds);
        }

        Optional.ofNullable(request.getTitle()).ifPresent(compilation::setTitle);
        Optional.ofNullable(request.getPinned()).ifPresent(compilation::setPinned);

        Compilation updatedComp = compilationRepository.save(compilation);
        log.info("Updated compilation with id: {}", updatedComp.getId());

        // Запрашиваем просмотры событий и создаём дто для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(events);
        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events, viewsMap);

        return compilationMapper.convertToDto(updatedComp, eventDtos);
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Deleted compilation with id: {}", compId);
    }
}
