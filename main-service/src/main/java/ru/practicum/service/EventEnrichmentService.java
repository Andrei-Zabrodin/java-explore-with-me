package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventEnrichmentService {

    private final ViewsService viewsService;
    private final EventMapper eventMapper;

    public EventFullDto enrichFull(Event event) {
        return enrichFull(event, null, null);
    }

    public EventShortDto enrichShort(Event event) {
        return enrichShort(event, null, null);
    }

    public List<EventShortDto> enrichShortList(List<Event> events) {
        return enrichShortList(events, null, null);
    }

    public List<EventFullDto> enrichFullList(List<Event> events) {
        return enrichFullList(events, null, null);
    }

    public EventFullDto enrichFull(Event event, LocalDateTime start, LocalDateTime end) {
        EventFullDto dto = eventMapper.convertToFullDto(event);

        Long views = viewsService.getViewsForEvent(event, start, end);
        dto.setViews(views);

        return dto;
    }

    public EventShortDto enrichShort(Event event, LocalDateTime start, LocalDateTime end) {
        EventShortDto dto = eventMapper.convertToShortDto(event);

        Long views = viewsService.getViewsForEvent(event, start, end);
        dto.setViews(views);

        return dto;
    }

    public List<EventShortDto> enrichShortList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = viewsService.getViewsMap(events, start, end);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.convertToShortDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<EventFullDto> enrichFullList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = viewsService.getViewsMap(events, start, end);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.convertToFullDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }
}