package ru.practicum.controller.priv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.priv.PrivateEventService;
import ru.practicum.service.priv.PrivateRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Slf4j
@Validated
public class PrivateEventController {
    private final PrivateEventService privateEventService;
    private final PrivateRequestService privateRequestService;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("GET /users/{}/events - получение событий пользователя", userId);
        return privateEventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{} - получение события пользователем", userId, eventId);
        return privateEventService.getEventById(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEventOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests - получение заявок на событие", userId, eventId);
        return privateRequestService.getRequestsByEventOwner(userId, eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto dto) {
        log.info("POST /users/{}/events - создание события пользователем", userId);
        return privateEventService.createEvent(userId, dto);
    }

    @PostMapping("/locations/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEventInLocation(@PathVariable Long userId, @PathVariable Long id,
                                    @Valid @RequestBody NewEventDto dto) {
        log.info("POST /users/{}/events/locations/{} - создание события пользователем", userId, id);
        return privateEventService.createEventInOfficialLocation(userId, id, dto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {
        log.info("PATCH /users/{}/events/{} - обновление события пользователем", userId, eventId);
        return privateEventService.updateEvent(userId, eventId, request);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH /users/{}/events/{}/requests - изменение статуса заявок", userId, eventId);
        return privateRequestService.updateRequestStatus(userId, eventId, request);
    }

}
