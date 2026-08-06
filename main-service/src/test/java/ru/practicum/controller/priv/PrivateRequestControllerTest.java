package ru.practicum.controller.priv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.request.RequestState;
import ru.practicum.service.priv.PrivateRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivateRequestController.class)
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrivateRequestService privateRequestService;

    private Long userId;
    private Long requestId;
    private Long eventId;
    private ParticipationRequestDto participationRequestDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 1L;
        eventId = 1L;
        now = LocalDateTime.now();

        participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(requestId);
        participationRequestDto.setEvent(eventId);
        participationRequestDto.setRequester(userId);
        participationRequestDto.setCreated(now);
        participationRequestDto.setStatus(RequestState.PENDING);
    }

    // ============ GET /users/{userId}/requests ============

    @Test
    void getRequestsByUserShouldReturnListWhenValidRequest() throws Exception {
        List<ParticipationRequestDto> requests = List.of(participationRequestDto);

        when(privateRequestService.getRequestsByUser(eq(userId)))
                .thenReturn(requests);

        mockMvc.perform(get("/users/{userId}/requests", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$[0].event").value(participationRequestDto.getEvent()))
                .andExpect(jsonPath("$[0].status").value(participationRequestDto.getStatus().name()));

        verify(privateRequestService, times(1))
                .getRequestsByUser(eq(userId));
    }

    @Test
    void getRequestsByUserShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(get("/users/{userId}/requests", "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .getRequestsByUser(anyLong());
    }

    // ============ POST /users/{userId}/requests ============

    @Test
    void createRequestShouldReturnCreatedWhenValidRequest() throws Exception {
        when(privateRequestService.createRequest(eq(userId), eq(eventId)))
                .thenReturn(participationRequestDto);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$.event").value(participationRequestDto.getEvent()))
                .andExpect(jsonPath("$.status").value(participationRequestDto.getStatus().name()));

        verify(privateRequestService, times(1))
                .createRequest(eq(userId), eq(eventId));
    }

    @Test
    void createRequestShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", "invalid")
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .createRequest(anyLong(), anyLong());
    }

    @Test
    void createRequestShouldReturnBadRequestWhenInvalidEventId() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .createRequest(anyLong(), anyLong());
    }

    @Test
    void createRequestShouldReturnBadRequestWhenNoEventId() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", userId))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .createRequest(anyLong(), anyLong());
    }

    // ============ PATCH /users/{userId}/requests/{requestId}/cancel ============

    @Test
    void cancelRequestShouldReturnOkWhenValidRequest() throws Exception {
        ParticipationRequestDto canceledRequest = new ParticipationRequestDto();
        canceledRequest.setId(requestId);
        canceledRequest.setEvent(eventId);
        canceledRequest.setRequester(userId);
        canceledRequest.setCreated(now);
        canceledRequest.setStatus(RequestState.CANCELED);

        when(privateRequestService.cancelRequest(eq(userId), eq(requestId)))
                .thenReturn(canceledRequest);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canceledRequest.getId()))
                .andExpect(jsonPath("$.status").value(canceledRequest.getStatus().name()));

        verify(privateRequestService, times(1))
                .cancelRequest(eq(userId), eq(requestId));
    }

    @Test
    void cancelRequestShouldReturnBadRequestWhenInvalidUserId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", "invalid", requestId))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .cancelRequest(anyLong(), anyLong());
    }

    @Test
    void cancelRequestShouldReturnBadRequestWhenInvalidRequestId() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, "invalid"))
                .andExpect(status().isBadRequest());

        verify(privateRequestService, never())
                .cancelRequest(anyLong(), anyLong());
    }
}