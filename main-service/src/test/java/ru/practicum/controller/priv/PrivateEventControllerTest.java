package ru.practicum.controller.priv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewCoordinatesDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.RequestState;
import ru.practicum.model.event.UserStateAction;
import ru.practicum.service.priv.PrivateEventService;
import ru.practicum.service.priv.PrivateRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivateEventController.class)
class PrivateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrivateEventService privateEventService;

    @MockBean
    private PrivateRequestService privateRequestService;

    private Long userId;
    private Long eventId;
    private Long requestId;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateEventUserRequest;
    private EventRequestStatusUpdateRequest statusUpdateRequest;
    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private ParticipationRequestDto participationRequestDto;
    private EventRequestStatusUpdateResult statusUpdateResult;
    private LocationDto locationDto;
    private NewCoordinatesDto coordinatesDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userId = 1L;
        eventId = 1L;
        requestId = 1L;
        now = LocalDateTime.now();

        locationDto = new LocationDto();
        locationDto.setLat(55.75);
        locationDto.setLon(37.62);

        coordinatesDto = new NewCoordinatesDto();
        coordinatesDto.setLat(55.75);
        coordinatesDto.setLon(37.62);

        newEventDto = new NewEventDto();
        newEventDto.setTitle("Тестовое событие");
        newEventDto.setAnnotation("Тестовая аннотация для события");
        newEventDto.setDescription("Тестовое описание события для проверки");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(now.plusDays(5));
        newEventDto.setLocation(coordinatesDto);
        newEventDto.setPaid(false);
        newEventDto.setParticipantLimit(10);
        newEventDto.setRequestModeration(true);

        updateEventUserRequest = new UpdateEventUserRequest();
        updateEventUserRequest.setTitle("Обновлённое событие");
        updateEventUserRequest.setAnnotation("Обновлённая аннотация");
        updateEventUserRequest.setDescription("Обновлённое описание");
        updateEventUserRequest.setCategory(1L);
        updateEventUserRequest.setEventDate(now.plusDays(7));
        updateEventUserRequest.setLocation(locationDto);
        updateEventUserRequest.setPaid(true);
        updateEventUserRequest.setParticipantLimit(20);
        updateEventUserRequest.setRequestModeration(false);
        updateEventUserRequest.setStateAction(UserStateAction.SEND_TO_REVIEW);

        statusUpdateRequest = new EventRequestStatusUpdateRequest();
        statusUpdateRequest.setRequestIds(List.of(1L, 2L, 3L));
        statusUpdateRequest.setStatus(RequestState.CONFIRMED);

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
        eventFullDto.setState(EventState.PENDING);
        eventFullDto.setPaid(false);
        eventFullDto.setParticipantLimit(10);
        eventFullDto.setRequestModeration(true);
        eventFullDto.setConfirmedRequests(0L);

        participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(requestId);
        participationRequestDto.setEvent(eventId);
        participationRequestDto.setRequester(userId);
        participationRequestDto.setCreated(now);
        participationRequestDto.setStatus(RequestState.PENDING);

        statusUpdateResult = new EventRequestStatusUpdateResult();
        statusUpdateResult.setConfirmedRequests(List.of(participationRequestDto));
        statusUpdateResult.setRejectedRequests(List.of());
    }

    // ============ GET /users/{userId}/events ============

    @Test
    void getEventsByUserShouldReturnListWhenValidRequest() throws Exception {
        List<EventShortDto> events = List.of(eventShortDto);

        when(privateEventService.getEventsByUser(eq(userId), anyInt(), anyInt()))
                .thenReturn(events);

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));

        verify(privateEventService, times(1))
                .getEventsByUser(eq(userId), anyInt(), anyInt());
    }

    @Test
    void getEventsByUserShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(get("/users/{userId}/events", "invalid")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .getEventsByUser(eq(userId), anyInt(), anyInt());
    }

    @Test
    void getEventsByUserShouldReturnBadRequestWhenFromIsInvalid() throws Exception {
        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .getEventsByUser(anyLong(), anyInt(), anyInt());
    }

    // ============ GET /users/{userId}/events/{eventId} ============

    @Test
    void getEventByIdShouldReturnEventWhenValidRequest() throws Exception {
        when(privateEventService.getEventById(eq(userId), eq(eventId)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()))
                .andExpect(jsonPath("$.state").value(eventFullDto.getState().name()));

        verify(privateEventService, times(1))
                .getEventById(eq(userId), eq(eventId));
    }

    @Test
    void getEventsByIdShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .getEventById(anyLong(), anyLong());
    }

    // ============ GET /users/{userId}/events/{eventId}/requests ============

    @Test
    void getRequestsByEventOwnerShouldReturnListWhenValidRequest() throws Exception {
        List<ParticipationRequestDto> requests = List.of(participationRequestDto);

        when(privateRequestService.getRequestsByEventOwner(eq(userId), eq(eventId)))
                .thenReturn(requests);

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$[0].status").value(participationRequestDto.getStatus().name()));

        verify(privateRequestService, times(1))
                .getRequestsByEventOwner(eq(userId), eq(eventId));
    }

    @Test
    void getRequestsByEventOwnerShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", "invalid", eventId))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .getRequestsByEventOwner(anyLong(), anyLong());
    }

    @Test
    void getRequestsByEventOwnerShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .getRequestsByEventOwner(anyLong(), anyLong());
    }

    // ============ POST /users/{userId}/events ============

    @Test
    void createEventShouldReturnCreatedWhenValidRequest() throws Exception {
        when(privateEventService.createEvent(eq(userId), any(NewEventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        verify(privateEventService, times(1))
                .createEvent(eq(userId), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        newEventDto.setTitle("");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenTitleIsTooShort() throws Exception {
        newEventDto.setTitle("AB");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        newEventDto.setTitle("A".repeat(130));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenAnnotationIsBlank() throws Exception {
        newEventDto.setAnnotation("");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenAnnotationIsTooShort() throws Exception {
        newEventDto.setAnnotation("Short");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenAnnotationIsTooLong() throws Exception {
        newEventDto.setAnnotation("A".repeat(2100));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        newEventDto.setDescription("");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenDescriptionIsTooShort() throws Exception {
        newEventDto.setDescription("Short");

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        newEventDto.setDescription("A".repeat(7100));

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenCategoryIsNull() throws Exception {
        newEventDto.setCategory(null);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenEventDateIsNull() throws Exception {
        newEventDto.setEventDate(null);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenLocationIsNull() throws Exception {
        newEventDto.setLocation(null);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    @Test
    void createEventShouldReturnBadRequestWhenParticipantLimitIsNegative() throws Exception {
        newEventDto.setParticipantLimit(-1);

        mockMvc.perform(post("/users/{userId}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .createEvent(anyLong(), any(NewEventDto.class));
    }

    // ============ PATCH /users/{userId}/events/{eventId} ============

    @Test
    void updateEventShouldReturnOkWhenValidRequest() throws Exception {
        EventFullDto updatedDto = new EventFullDto();
        updatedDto.setId(eventId);
        updatedDto.setTitle("Обновлённое событие");
        updatedDto.setState(EventState.PENDING);

        when(privateEventService.updateEvent(eq(userId), eq(eventId), any(UpdateEventUserRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDto.getId()))
                .andExpect(jsonPath("$.title").value(updatedDto.getTitle()));

        verify(privateEventService, times(1))
                .updateEvent(eq(userId), eq(eventId), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenTitleIsTooShort() throws Exception {
        updateEventUserRequest.setTitle("A");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        updateEventUserRequest.setTitle("A".repeat(130));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenAnnotationIsTooShort() throws Exception {
        updateEventUserRequest.setAnnotation("Short");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenAnnotationIsTooLong() throws Exception {
        updateEventUserRequest.setAnnotation("A".repeat(2100));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenDescriptionIsTooShort() throws Exception {
        updateEventUserRequest.setDescription("Short");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        updateEventUserRequest.setDescription("A".repeat(7100));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenInvalidId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", "invalid", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenEventDateFormatIsInvalid() throws Exception {
        String invalidJson = "{\"title\": \"Обновлённое событие\", \"eventDate\": \"2026-07-27T10:30:00\"}";

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenInvalidStateAction() throws Exception {
        String invalidJson = "{\"title\": \"Обновлённое событие\", \"stateAction\": \"INVALID_STATE\"}";

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEventShouldReturnBadRequestWhenParticipantLimitIsNegative() throws Exception {
        updateEventUserRequest.setParticipantLimit(-1);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isBadRequest());

        verify(privateEventService, never())
                .updateEvent(anyLong(), anyLong(), any(UpdateEventUserRequest.class));
    }

    // ============ PATCH /users/{userId}/events/{eventId}/requests ============

    @Test
    void updateRequestStatusShouldReturnOkWhenValidRequest() throws Exception {
        when(privateRequestService.updateRequestStatus(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(statusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()").value(1))
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$.rejectedRequests.length()").value(0));

        verify(privateRequestService, times(1))
                .updateRequestStatus(eq(userId), eq(eventId), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenRequestIdsIsEmpty() throws Exception {
        statusUpdateRequest.setRequestIds(List.of());

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenStatusIsNull() throws Exception {
        statusUpdateRequest.setStatus(null);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenStatusIsInvalid() throws Exception {
        String invalidJson = "{\"requestIds\": [1, 2, 3], \"status\": \"INVALID_STATUS\"}";

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", "invalid", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void updateRequestStatusShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .updateRequestStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class));
    }
}