package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewsService {

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
}