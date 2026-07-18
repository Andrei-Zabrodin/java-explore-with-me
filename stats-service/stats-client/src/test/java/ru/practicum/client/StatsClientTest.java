package ru.practicum.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StatsClient statsClient;

    private static final String SERVER_URL = "http://localhost:9090";

    private EndpointHitDto hitDto;
    private ViewStatsDto viewStatsDto;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        statsClient = new StatsClient(new RestTemplateBuilder());
        ReflectionTestUtils.setField(statsClient, "rest", restTemplate);
        ReflectionTestUtils.setField(statsClient, "serverUrl", SERVER_URL);

        start = LocalDateTime.of(2026, 7, 18, 10, 0, 0);
        end = LocalDateTime.of(2026, 7, 18, 20, 0, 0);

        hitDto = new EndpointHitDto();
        hitDto.setApp("test-app");
        hitDto.setUri("/events/1");
        hitDto.setIp("127.0.0.1");
        hitDto.setTimestamp(LocalDateTime.now());

        viewStatsDto = new ViewStatsDto();
        viewStatsDto.setApp("test-app");
        viewStatsDto.setUri("/events/1");
        viewStatsDto.setHits(10L);
    }

    @Test
    void getStatsShouldReturnStatsList() {
        ViewStatsDto[] expectedStats = {viewStatsDto};
        ResponseEntity<ViewStatsDto[]> responseEntity = new ResponseEntity<>(
                expectedStats,
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        )).thenReturn(responseEntity);

        List<ViewStatsDto> result = statsClient.getStats(start, end, List.of("/events/1"), false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApp()).isEqualTo(viewStatsDto.getApp());
        assertThat(result.get(0).getUri()).isEqualTo(viewStatsDto.getUri());
        assertThat(result.get(0).getHits()).isEqualTo(viewStatsDto.getHits());

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        );
    }

    @Test
    void getStatsShouldReturnEmptyListWhenResponseBodyIsNull() {
        ResponseEntity<ViewStatsDto[]> responseEntity = new ResponseEntity<>(
                null,
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        )).thenReturn(responseEntity);

        List<ViewStatsDto> result = statsClient.getStats(start, end, List.of("/events/1"), false);

        assertThat(result).isEmpty();
    }

    @Test
    void getStatsShouldReturnEmptyListWhenResponseIsNotSuccessful() {
        ResponseEntity<ViewStatsDto[]> responseEntity = new ResponseEntity<>(
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        )).thenReturn(responseEntity);

        List<ViewStatsDto> result = statsClient.getStats(start, end, List.of("/events/1"), false);

        assertThat(result).isEmpty();
    }

    @Test
    void getStatsShouldHandleNullUri() {
        ViewStatsDto[] expectedStats = {viewStatsDto};
        ResponseEntity<ViewStatsDto[]> responseEntity = new ResponseEntity<>(
                expectedStats,
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        )).thenReturn(responseEntity);

        List<ViewStatsDto> result = statsClient.getStats(start, end, null, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApp()).isEqualTo(viewStatsDto.getApp());
        assertThat(result.get(0).getUri()).isEqualTo(viewStatsDto.getUri());
        assertThat(result.get(0).getHits()).isEqualTo(viewStatsDto.getHits());
    }

    @Test
    void getStatsShouldHandleEmptyUris() {
        ViewStatsDto[] expectedStats = {viewStatsDto};
        ResponseEntity<ViewStatsDto[]> responseEntity = new ResponseEntity<>(
                expectedStats,
                HttpStatus.OK
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ViewStatsDto[].class)
        )).thenReturn(responseEntity);

        List<ViewStatsDto> result = statsClient.getStats(start, end, List.of(), false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApp()).isEqualTo(viewStatsDto.getApp());
        assertThat(result.get(0).getUri()).isEqualTo(viewStatsDto.getUri());
        assertThat(result.get(0).getHits()).isEqualTo(viewStatsDto.getHits());
    }

    @Test
    void sendHitShouldSendHit() {
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(SERVER_URL + "/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(responseEntity);

        statsClient.sendHit(hitDto);

        verify(restTemplate, times(1)).exchange(
                eq(SERVER_URL + "/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void sendHitsShouldHandleNullHitDto() {
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(SERVER_URL + "/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(responseEntity);

        statsClient.sendHit(null);

        verify(restTemplate, times(1)).exchange(
                eq(SERVER_URL + "/hit"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}