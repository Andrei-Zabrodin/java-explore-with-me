package ru.practicum.service.priv;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

import java.util.List;

public interface PrivateEventService {
    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);
}
