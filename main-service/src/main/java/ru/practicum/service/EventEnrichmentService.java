package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.RequestState;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventEnrichmentService {

    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
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

        Long views = getViewsForEvent(event, start, end);
        dto.setViews(views);

        Long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestState.CONFIRMED);
        dto.setConfirmedRequests(confirmed);

        return dto;
    }

    public EventShortDto enrichShort(Event event, LocalDateTime start, LocalDateTime end) {
        EventShortDto dto = eventMapper.convertToShortDto(event);

        Long views = getViewsForEvent(event, start, end);
        dto.setViews(views);

        Long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestState.CONFIRMED);
        dto.setConfirmedRequests(confirmed);

        return dto;
    }

    public List<EventShortDto> enrichShortList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = getViewsMap(events, start, end);
        Map<Long, Long> confirmedMap = getConfirmedMap(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.convertToShortDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<EventFullDto> enrichFullList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = getViewsMap(events, start, end);
        Map<Long, Long> confirmedMap = getConfirmedMap(events);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.convertToFullDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    private Long getViewsForEvent(Event event, LocalDateTime start, LocalDateTime end) {
        String uri = "/events/" + event.getId();
        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), true);
        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }

    private Map<String, Long> getViewsMap(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
    }

    private Map<Long, Long> getConfirmedMap(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> requestRepository.countByEventIdAndStatus(event.getId(), RequestState.CONFIRMED)
                ));
    }
}