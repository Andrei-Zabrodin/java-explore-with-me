package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewsService {

    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    public void sendHit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("main-service");
        hitDto.setUri(uri);
        hitDto.setIp(ip);
        hitDto.setTimestamp(LocalDateTime.now());

        statsClient.sendHit(hitDto);
        log.debug("Отправлен hit в stats-service: uri={}, ip={}", uri, ip);
    }

    public EventFullDto createFullDtoWithViews(Event event, LocalDateTime start, LocalDateTime end) {
        long views = getViewsForEvent(event, start, end);

        EventFullDto dto = eventMapper.convertToFullDto(event);
        dto.setViews(views);

        return dto;
    }

    public EventFullDto createFullDtoWithViews(Event event) {
        return createFullDtoWithViews(event, null, null);
    }

    public List<EventShortDto> createShortDtosWithViews(List<Event> events,
                                                         LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = getViewsForEvents(events, start, end);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.convertToShortDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<EventShortDto> createShortDtosWithViews(List<Event> events) {
        return createShortDtosWithViews(events, null, null);
    }

    // Получить просмотры для списка событий
    private Map<String, Long> getViewsForEvents(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
    }

    // Получить просмотры для одного события
    private Long getViewsForEvent(Event event, LocalDateTime start, LocalDateTime end) {
        if (event == null) {
            return 0L;
        }

        String uri = "/events/" + event.getId();
        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), true);

        if (stats.isEmpty()) {
            return 0L;
        }

        return stats.getFirst().getHits();
    }
}