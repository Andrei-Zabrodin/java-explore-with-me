package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewsService {
    private static final LocalDateTime DEFAULT_START = LocalDateTime.of(1970, 1, 1, 0, 0,0);

    private final StatsClient statsClient;

    public void sendHit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("ewm-main-service");
        hitDto.setUri(uri);
        hitDto.setIp(ip);
        hitDto.setTimestamp(LocalDateTime.now());

        statsClient.sendHit(hitDto);
        log.debug("Отправлен hit в stats-service: uri={}, ip={}", uri, ip);
    }

    public Long getViewsForEvent(Event event, LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = DEFAULT_START;
        }

        if (end == null) {
            end = LocalDateTime.now();
        }

        String uri = "/events/" + event.getId();
        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), true);
        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }

    public Map<String, Long> getViewsMap(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        if (start == null) {
            start = DEFAULT_START;
        }

        if (end == null) {
            end = LocalDateTime.now();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
    }
}