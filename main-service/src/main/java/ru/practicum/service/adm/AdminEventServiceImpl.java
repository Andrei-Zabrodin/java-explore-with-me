package ru.practicum.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.location.Location;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.service.ViewsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final ViewsService viewsService;

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Double lat, Double lon, Double radius, int from, int size) {

        List<EventState> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            try {
                stateEnums = states.stream()
                        .map(EventState::valueOf)
                        .toList();
            } catch (Exception e) {
                throw new ValidationException("Unknown state: " + e.getMessage());
            }
        }

        if (lat != null && lon != null) {
            validateCoordinates(lat, lon);
            if (radius > 1000) {
                throw new ValidationException("Radius cannot exceed 1000 km");
            }
        } else if (lat != null || lon != null) {
            throw new ValidationException("Latitude and longitude must be provided together");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> page = eventRepository.findEventsByAdminFilters(
                users,
                stateEnums,
                categories,
                rangeStart,
                rangeEnd,
                lat,
                lon,
                radius,
                pageable
        );

        // Запрашиваем просмотры для событий
        Map<String, Long> viewsMap = viewsService.getViewsForEventList(page.getContent(), rangeStart, rangeEnd);

        return eventMapper.convertToFullDtoList(page.getContent(), viewsMap);
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    // Проверяем, что событие не раньше чем через час от текущего момента
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event date must be at least one hour from now");
                    }

                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;

                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's not in the right state: " + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                    break;

                default:
                    throw new ValidationException("Unknown State Action");
            }
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getEventDate() != null) {
            // Если событие уже опубликовано и мы меняем время начала,
            // то проверяем, что событие начнётся не раньше чем через час от даты публикации
            if (event.getState() == EventState.PUBLISHED
                    && request.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Event date must be at least one hour after the publication date");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null && request.getLocation().getId() != null) {
            Location location = locationRepository.findById(request.getLocation().getId())
                    .orElseThrow(() -> new NotFoundException("Location with id=" + request.getLocation().getId() + " was not found"));
            event.setLocation(location);
        }

        Optional.ofNullable(request.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(request.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(request.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(request.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(request.getTitle()).ifPresent(event::setTitle);

        Event updatedEvent = eventRepository.save(event);
        log.info("Admin updated event with id: {}", updatedEvent.getId());

        // Запрашиваем просмотры для события
        Long views = viewsService.getViewsForEvent(updatedEvent);

        return eventMapper.convertToFullDto(updatedEvent, views);
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
