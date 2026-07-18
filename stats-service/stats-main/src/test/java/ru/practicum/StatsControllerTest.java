package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EndpointHitDto hitDto;
    private ViewStatsDto viewStatsDto;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
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

    // ============ GET /stats ============

    @Test
    void getStatsShouldReturnStatsList() throws Exception {
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(List.of(viewStatsDto));

        mockMvc.perform(get("/stats")
                        .param("start", start.format(FORMATTER))
                        .param("end", end.format(FORMATTER))
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(viewStatsDto.getApp()))
                .andExpect(jsonPath("$[0].uri").value(viewStatsDto.getUri()))
                .andExpect(jsonPath("$[0].hits").value(viewStatsDto.getHits()));

        verify(statsService, times(1))
                .getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean());
    }

    @Test
    void getStatsShouldReturnStatsListWhenUrisIsEmpty() throws Exception {
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), isNull(), anyBoolean()))
                .thenReturn(List.of(viewStatsDto));

        mockMvc.perform(get("/stats")
                        .param("start", start.format(FORMATTER))
                        .param("end", end.format(FORMATTER))
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(viewStatsDto.getApp()))
                .andExpect(jsonPath("$[0].uri").value(viewStatsDto.getUri()))
                .andExpect(jsonPath("$[0].hits").value(viewStatsDto.getHits()));

        verify(statsService, times(1))
                .getStats(any(LocalDateTime.class), any(LocalDateTime.class), isNull(), anyBoolean());
    }

    @Test
    void getStatsShouldReturnStatsListWhenUniqueIsTrue() throws Exception {
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(viewStatsDto));

        mockMvc.perform(get("/stats")
                        .param("start", start.format(FORMATTER))
                        .param("end", end.format(FORMATTER))
                        .param("uris", "/events/1")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(viewStatsDto.getApp()))
                .andExpect(jsonPath("$[0].uri").value(viewStatsDto.getUri()))
                .andExpect(jsonPath("$[0].hits").value(viewStatsDto.getHits()));

        verify(statsService, times(1))
                .getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true));
    }

    @Test
    void getStatsShouldReturnBadRequestWhenStartIsMissing() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", end.format(FORMATTER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatsShouldReturnBadRequestWhenEndIsMissing() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", start.format(FORMATTER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatsShouldReturnBadRequestWhenStartHasInvalidFormat() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2026-07-18")
                        .param("end", end.format(FORMATTER)))
                .andExpect(status().isBadRequest());
    }

    // ============ POST /hit ============

    @Test
    void postStatsShouldReturnCreated() throws Exception {
        doNothing().when(statsService).saveHit(any(EndpointHitDto.class));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated());

        verify(statsService, times(1)).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void postStatsShouldReturnBadRequestWhenAppIsBlank() throws Exception {
        hitDto.setApp("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void postStatsShouldReturnBadRequestWhenUriIsBlank() throws Exception {
        hitDto.setUri("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void postStatsShouldReturnBadRequestWhenIpIsBlank() throws Exception {
        hitDto.setIp("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void postStatsShouldReturnBadRequestWhenTimestampIsNull() throws Exception {
        hitDto.setTimestamp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isBadRequest());

        verify(statsService, never()).saveHit(any(EndpointHitDto.class));
    }
}
