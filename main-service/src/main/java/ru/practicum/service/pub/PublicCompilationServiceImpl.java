package ru.practicum.service.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
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

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PublicCompilationServiceImpl extends BaseCompilationService implements PublicCompilationService {

    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final ViewsService viewsService;

    public PublicCompilationServiceImpl(CompilationRepository compilationRepository,
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
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        if (compilations.isEmpty()) {
            return List.of();
        }

        log.debug("Найдено подборок: {}", compilations.size());

        Map<Long, List<Long>> compilationEventsMap = getCompilationEventsMap(compilations);
        // Получаем список id событий и ищем соответствующие события
        List<Long> eventIds = compilationEventsMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<Event> events = eventRepository.findAllByIdWithFetch(eventIds);

        // Запрашиваем просмотры событий и создаём дто для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(events);
        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events, viewsMap);

        return compilationMapper.convertToDtoList(compilations, eventDtos, compilationEventsMap);
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        // Получаем список id событий и ищем соответствующие события
        List<Long> eventIds = getCompilationEventsList(compilation);
        List<Event> events = eventRepository.findAllByIdWithFetch(eventIds);

        // Запрашиваем просмотры событий и создаём дто для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(events);
        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events, viewsMap);

        return compilationMapper.convertToDto(compilation, eventDtos);
    }
}
