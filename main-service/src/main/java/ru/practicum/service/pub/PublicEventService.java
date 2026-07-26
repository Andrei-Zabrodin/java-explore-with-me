package ru.practicum.service.pub;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.SortByType;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, SortByType sortBy,
                                  int from, int size, HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);
}
