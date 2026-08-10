package ru.practicum.service.pub;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.location.Location;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.SortByType;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.service.ViewsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ViewsService viewsService;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortByType sortBy,
                                         Double lat, Double lon, Double radius,
                                         int from, int size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала диапазона выгрузки должна быть не позже даты конца");
        }

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        if (lat != null && lon != null) {
            validateCoordinates(lat, lon);

            if (radius > 1000) {
                throw new ValidationException("Radius cannot exceed 1000 km");
            }
        } else if (lat != null || lon != null) {
            throw new ValidationException("Latitude and longitude must be provided together");
        }

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(from / size, size, sort);
        Page<Event> page = eventRepository.findEventsByPublicFilters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, lat, lon, radius, pageable);
        List<Event> events = page.getContent();

        // Отправляем информацию в сервис статистики
        viewsService.sendHit(request);

        // Запрашиваем просмотры для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(page.getContent(), rangeStart, rangeEnd);

        return eventMapper.convertToShortDtoList(events, viewsMap);
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " is not yet published");
        }

        // Отправляем информацию в сервис статистики
        viewsService.sendHit(request);

        // Запрашиваем просмотры для события
        Long views = viewsService.getViewsForEvent(event);

        return eventMapper.convertToFullDto(event, views);
    }

    @Override
    public List<EventShortDto> getEventsByLocationId(Long locationId, String text, List<Long> categories,
                                                     Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable, SortByType sortBy,
                                                     int from, int size, HttpServletRequest request) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location with id=" + locationId + " was not found"));

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }

        Double lat = location.getLat();
        Double lon = location.getLon();
        Double radius = 0.0; // Ищем только в самой локации

        Sort sort = getSort(sortBy);
        Pageable pageable = PageRequest.of(from / size, size, sort);
        Page<Event> page = eventRepository.findEventsByPublicFilters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, lat, lon, radius, pageable);

        List<Event> events = page.getContent();

        // Отправляем информацию в сервис статистики
        viewsService.sendHit(request);

        // Запрашиваем просмотры для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(events, rangeStart, rangeEnd);

        return eventMapper.convertToShortDtoList(events, viewsMap);
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

    private void validateCoordinates(Double lat, Double lon) {
        if (lat < -90 || lat > 90) {
            throw new ValidationException("Latitude must be in range from -90° to 90°");
        }
        if (lon < -180 || lon > 180) {
            throw new ValidationException("Longitude must be in range from -180° to 180°");
        }
    }
}
