package ru.practicum.controller.adm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.event.EventState;
import ru.practicum.model.Location;
import ru.practicum.service.adm.AdminEventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminEventService adminEventService;

    private UpdateEventAdminRequest updateEventAdminRequest;
    private EventFullDto eventFullDto;
    private Long eventId;
    private LocalDateTime now;
    private Location location;

    @BeforeEach
    void setUp() {
        eventId = 1L;
        now = LocalDateTime.now();

        location = new Location();
        location.setLat(55.75);
        location.setLon(37.62);

        updateEventAdminRequest = new UpdateEventAdminRequest();
        updateEventAdminRequest.setTitle("Обновлённое событие");
        updateEventAdminRequest.setAnnotation("Обновлённая аннотация события");
        updateEventAdminRequest.setDescription("Обновлённое описание события");
        updateEventAdminRequest.setCategory(1L);
        updateEventAdminRequest.setEventDate(now.plusDays(5));
        updateEventAdminRequest.setLocation(location);
        updateEventAdminRequest.setPaid(true);
        updateEventAdminRequest.setParticipantLimit(10);
        updateEventAdminRequest.setRequestModeration(true);

        eventFullDto = new EventFullDto();
        eventFullDto.setId(eventId);
        eventFullDto.setTitle("Обновлённое событие");
        eventFullDto.setAnnotation("Обновлённая аннотация события");
        eventFullDto.setDescription("Обновлённое описание события");
        eventFullDto.setState(EventState.PENDING);
    }

    // ============ GET /admin/events ============

    @Test
    void getEventsShouldReturnListWhenValidRequest() throws Exception {
        List<EventFullDto> events = List.of(eventFullDto);

        when(adminEventService.getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(events);

        mockMvc.perform(get("/admin/events")
                        .param("users", "1", "2")
                        .param("states", "PENDING", "PUBLISHED")
                        .param("categories", "1", "2")
                        .param("rangeStart", "2026-07-01 00:00:00")
                        .param("rangeEnd", "2026-07-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventFullDto.getTitle()));

        verify(adminEventService, times(1))
                .getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void getEventsShouldReturnBadRequestWhenFromIsInvalid() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .getEvents(anyList(), anyList(), anyList(), any(), any(), anyInt(), anyInt());
    }

    // ============ PATCH /admin/events/{eventId} ============

    @Test
    void updateEventShouldReturnOkWhenValidRequest() throws Exception {
        EventFullDto updatedDto = new EventFullDto();
        updatedDto.setId(eventId);
        updatedDto.setTitle("Обновлённое событие");
        updatedDto.setState(EventState.PUBLISHED);

        when(adminEventService.updateEvent(eq(eventId), any(UpdateEventAdminRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDto.getId()))
                .andExpect(jsonPath("$.title").value(updatedDto.getTitle()))
                .andExpect(jsonPath("$.state").value(updatedDto.getState().name()));

        verify(adminEventService, times(1))
                .updateEvent(eq(eventId), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenTitleIsTooShort() throws Exception {
        updateEventAdminRequest.setTitle("A");

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        updateEventAdminRequest.setTitle("A".repeat(130));

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenAnnotationIsTooShort() throws Exception {
        updateEventAdminRequest.setAnnotation("A");

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenAnnotationIsTooLong() throws Exception {
        updateEventAdminRequest.setAnnotation("A".repeat(2100));

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenDescriptionIsTooShort() throws Exception {
        updateEventAdminRequest.setDescription("A");

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        updateEventAdminRequest.setDescription("A".repeat(7100));

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventAdminRequest)))
                .andExpect(status().isBadRequest());

        verify(adminEventService, never())
                .updateEvent(anyLong(), any(UpdateEventAdminRequest.class));
    }
}