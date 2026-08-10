package ru.practicum.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.event.EventState;
import ru.practicum.model.SortByType;
import ru.practicum.service.pub.PublicEventService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicEventController.class)
class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicEventService publicEventService;

    private Long eventId;
    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        eventId = 1L;
        now = LocalDateTime.now();

        eventShortDto = new EventShortDto();
        eventShortDto.setId(eventId);
        eventShortDto.setTitle("Тестовое событие");
        eventShortDto.setAnnotation("Тестовая аннотация");
        eventShortDto.setEventDate(now.plusDays(5));
        eventShortDto.setPaid(false);
        eventShortDto.setConfirmedRequests(0L);

        eventFullDto = new EventFullDto();
        eventFullDto.setId(eventId);
        eventFullDto.setTitle("Тестовое событие");
        eventFullDto.setAnnotation("Тестовая аннотация");
        eventFullDto.setDescription("Тестовое описание");
        eventFullDto.setEventDate(now.plusDays(5));
        eventFullDto.setCreatedOn(now);
        eventFullDto.setState(EventState.PUBLISHED);
        eventFullDto.setPaid(false);
        eventFullDto.setParticipantLimit(10);
        eventFullDto.setRequestModeration(true);
        eventFullDto.setConfirmedRequests(0L);
    }

    // ============ GET /events ============

    @Test
    void getEventsShouldReturnListWhenValidRequest() throws Exception {
        List<EventShortDto> events = List.of(eventShortDto);

        when(publicEventService.getEvents(
                anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(events);

        mockMvc.perform(get("/events")
                        .param("text", "концерт")
                        .param("categories", "1", "2")
                        .param("paid", "true")
                        .param("rangeStart", "2026-07-01 00:00:00")
                        .param("rangeEnd", "2026-07-31 23:59:59")
                        .param("onlyAvailable", "true")
                        .param("sortBy", "EVENT_DATE")
                        .param("lat", "55.00")
                        .param("lon", "37.00")
                        .param("radius", "100")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));

        verify(publicEventService, times(1))
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnListWithDefaultParameters() throws Exception {
        List<EventShortDto> events = List.of(eventShortDto);

        when(publicEventService.getEvents(isNull(), isNull(), isNull(), isNull(), isNull(), eq(false),
                isNull(), isNull(), isNull(), eq(10.0), eq(0), eq(10), any(HttpServletRequest.class)))
                .thenReturn(events);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());

        verify(publicEventService, times(1))
                .getEvents(isNull(), isNull(), isNull(), isNull(), isNull(), eq(false), isNull(),
                        isNull(), isNull(), eq(10.0), eq(0), eq(10), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenInvalidFrom() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenInvalidSize() throws Exception {
        mockMvc.perform(get("/events")
                        .param("size", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenNegativeFrom() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenZeroSize() throws Exception {
        mockMvc.perform(get("/events")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenInvalidSortBy() throws Exception {
        mockMvc.perform(get("/events")
                        .param("sortBy", "INVALID_SORT"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenInvalidRangeStartFormat() throws Exception {
        mockMvc.perform(get("/events")
                        .param("rangeStart", "2026-07-01"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    @Test
    void getEventsShouldReturnBadRequestWhenInvalidRangeEndFormat() throws Exception {
        mockMvc.perform(get("/events")
                        .param("rangeEnd", "2026-07-31"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEvents(anyString(), anyList(), anyBoolean(), any(), any(), anyBoolean(), any(SortByType.class),
                        anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt(), any(HttpServletRequest.class));
    }

    // ============ GET /events/{id} ============

    @Test
    void getEventByIdShouldReturnEventWhenExists() throws Exception {
        when(publicEventService.getEventById(eq(eventId), any(HttpServletRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.state").value(eventFullDto.getState().name()));

        verify(publicEventService, times(1))
                .getEventById(eq(eventId), any(HttpServletRequest.class));
    }

    @Test
    void getEventByIdShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(get("/events/{id}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicEventService, never())
                .getEventById(anyLong(), any(HttpServletRequest.class));
    }
}