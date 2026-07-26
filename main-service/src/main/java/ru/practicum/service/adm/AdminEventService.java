package ru.practicum.service.adm;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request);
}
