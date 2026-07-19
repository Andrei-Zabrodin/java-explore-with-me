package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class StatsClient {

    private final RestTemplate rest;

    @Value("${stats-server.url}")
    private String serverUrl;

    public StatsClient(RestTemplateBuilder builder) {
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .encode()
                .build()
                .toUriString();

        log.debug("Для запроса сформирован адрес: {}", url);

        ResponseEntity<ViewStatsDto[]> response;
        try {
            response = rest.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    ViewStatsDto[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Успешно получили статистику по запросам");
                return Arrays.asList(response.getBody());
            } else {
                log.warn("Не удалось запросить статистику, {}", response.getStatusCode());
                return List.of();
            }
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка при отправке запроса в stats-service: {}", e.getMessage());
            return List.of();
        }
    }

    public void sendHit(EndpointHitDto hitDto) {
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, createHeaders());

        ResponseEntity<Void> response;

        try {
            response = rest.exchange(
                    serverUrl + "/hit",
                    HttpMethod.POST,
                    request,
                    Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Успешно зарегистрировали запрос {}", hitDto);
            } else {
                log.warn("Не удалось зарегистрировать запрос {}", hitDto);
            }
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка при получении ответа от stats-service: {}", e.getMessage());
        }
    }


    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
