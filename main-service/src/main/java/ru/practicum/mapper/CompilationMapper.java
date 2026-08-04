package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CompilationMapper {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CompilationRepository compilationRepository;

    @Mapping(target = "events", ignore = true)
    public abstract Compilation convertToEntity(NewCompilationDto dto);

    public CompilationDto convertoToDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        List<Long> eventIds = getCompilationEventsList(compilation.getId());
        List<Event> events = eventRepository.findAllByIdWithFetch(eventIds);
        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events);

        CompilationDto dto = prepareForDto(compilation);
        dto.setEvents(eventDtos);

        return dto;
    }

    public List<CompilationDto> convertToDtoList(List<Compilation> compilations) {
        if (compilations == null || compilations.isEmpty()) {
            return List.of();
        }

        List<Long> compilationIds = compilations.stream()
                .map(Compilation::getId)
                .toList();

        Map<Long, List<Long>> compilationEventsMap = getCompilationEventsMap(compilationIds);

        List<Long> eventIds = compilationEventsMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<Event> events = eventRepository.findAllByIdWithFetch(eventIds);
        List<EventShortDto> eventDtoList = eventMapper.convertToShortDtoList(events);

        Map<Long, EventShortDto> eventDtoMap = eventDtoList.stream()
                .collect(Collectors.toMap(EventShortDto::getId, dto -> dto));

        return compilations.stream()
                .map(compilation -> {
                    CompilationDto dto = prepareForDto(compilation);
                    List<Long> compilationEventIds = compilationEventsMap.getOrDefault(compilation.getId(), List.of());
                    List<EventShortDto> compilationEvents = compilationEventIds.stream()
                            .map(eventDtoMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    dto.setEvents(compilationEvents);
                    return dto;
                })
                .toList();
    }

    public CompilationDto convertToCompilationDtoWithGivenEvents(Compilation compilation, List<Event> events) {
        if (compilation == null) {
            return null;
        }

        List<EventShortDto> eventDtos = eventMapper.convertToShortDtoList(events);

        CompilationDto dto = prepareForDto(compilation);
        dto.setEvents(eventDtos);

        return dto;
    }

    @Mapping(target = "events", ignore = true)
    protected abstract CompilationDto prepareForDto(Compilation entity);

    private Map<Long, List<Long>> getCompilationEventsMap(List<Long> compilationIds) {
        List<Object[]> results = compilationRepository.findCompilationEventIds(compilationIds);

        Map<Long, List<Long>> map = new HashMap<>();
        for (Object[] elem : results) {
            Long compilationId = (Long) elem[0];
            Long eventId = (Long) elem[1];
            map.computeIfAbsent(compilationId, k -> new ArrayList<>()).add(eventId);
        }

        return map;
    }

    private List<Long> getCompilationEventsList(Long compilationId) {
        List<Object[]> results = compilationRepository.findCompilationEventIds(List.of(compilationId));

        List<Long> eventIds = new ArrayList<>();
        for (Object[] elem : results) {
            Long eventId = (Long) elem[1];
            eventIds.add(eventId);
        }

        return eventIds;
    }
}
