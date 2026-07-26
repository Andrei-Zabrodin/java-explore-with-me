package ru.practicum.service.pub;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.SortByType;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.ViewsService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final ViewsService viewsService;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortByType sortBy,
                                         int from, int size, HttpServletRequest request) {

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(from / size, size, sort);
        Page<Event> page = eventRepository.findEventsByPublicFilters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, pageable);
        List<Event> events = page.getContent();

        // Отправляем информацию в сервис статистики
        viewsService.sendHit(request);

        return viewsService.createShortDtosWithViews(events, rangeStart, rangeEnd);
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " is not yet published");
        }

        // Отправляем статистику
        viewsService.sendHit(request);

        return viewsService.createFullDtoWithViews(event);
    }

    private Sort getSort(SortByType sortBy) {
        if (sortBy == null) {
            return Sort.unsorted();
        }

        return switch (sortBy) {
            case EVENT_DATE -> Sort.by("eventDate").ascending();
            case VIEWS -> Sort.by("views").descending();
        };
    }
}
