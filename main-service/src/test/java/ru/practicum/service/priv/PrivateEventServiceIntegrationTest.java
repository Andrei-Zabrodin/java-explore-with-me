package ru.practicum.service.priv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.model.Location;
import ru.practicum.model.event.UserStateAction;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PrivateEventServiceIntegrationTest {

    @Autowired
    private PrivateEventService privateEventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private User anotherUser;
    private Category category;
    private Event event;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateRequest;
    private Location location;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user = userRepository.save(user);

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setName("Another User");
        anotherUser = userRepository.save(anotherUser);

        category = new Category();
        category.setName("Category 1");
        category = categoryRepository.save(category);

        location = new Location();
        location.setLat(55.75);
        location.setLon(37.62);

        event = new Event();
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLat(55.75);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event = eventRepository.save(event);

        newEventDto = new NewEventDto();
        newEventDto.setTitle("New Event");
        newEventDto.setAnnotation("New Annotation");
        newEventDto.setDescription("New Description");
        newEventDto.setCategory(category.getId());
        newEventDto.setEventDate(LocalDateTime.now().plusDays(5));
        newEventDto.setLocation(location);
        newEventDto.setPaid(false);
        newEventDto.setParticipantLimit(10);
        newEventDto.setRequestModeration(true);

        updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated Event");
        updateRequest.setAnnotation("Updated Annotation");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategory(category.getId());
        updateRequest.setEventDate(LocalDateTime.now().plusDays(7));
        updateRequest.setLocation(location);
        updateRequest.setPaid(true);
        updateRequest.setParticipantLimit(20);
        updateRequest.setRequestModeration(false);
        updateRequest.setStateAction(UserStateAction.SEND_TO_REVIEW);
    }

    @Test
    void getEventsByUserShouldReturnListWhenUserExists() {
        List<EventShortDto> result = privateEventService.getEventsByUser(user.getId(), 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(event.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void getEventsByUserShouldReturnEmptyListWhenUserWithNoEvents() {
        List<EventShortDto> result = privateEventService.getEventsByUser(anotherUser.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByUserShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.getEventsByUser(nonExistentId, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventsByUserShouldReturnEventsWithPagination() {
        for (int i = 0; i < 5; i++) {
            Event extraEvent = new Event();
            extraEvent.setTitle("Extra Event " + i);
            extraEvent.setAnnotation("Annotation " + i);
            extraEvent.setDescription("Description " + i);
            extraEvent.setCategory(category);
            extraEvent.setConfirmedRequests(0L);
            extraEvent.setInitiator(user);
            extraEvent.setEventDate(LocalDateTime.now().plusDays(i + 10));
            extraEvent.setCreatedOn(LocalDateTime.now());
            extraEvent.setState(EventState.PENDING);
            extraEvent.setLat(55.75);
            extraEvent.setLon(37.62);
            extraEvent.setPaid(false);
            extraEvent.setParticipantLimit(10);
            extraEvent.setRequestModeration(true);
            eventRepository.save(extraEvent);
        }

        List<EventShortDto> firstPage = privateEventService.getEventsByUser(user.getId(), 0, 3);

        assertThat(firstPage).hasSize(3);
        assertThat(firstPage).extracting(EventShortDto::getTitle)
                .containsExactly(event.getTitle(), "Extra Event 0", "Extra Event 1");

        List<EventShortDto> secondPage = privateEventService.getEventsByUser(user.getId(), 3, 3);

        assertThat(secondPage).hasSize(3);
        assertThat(secondPage).extracting(EventShortDto::getTitle)
                .containsExactly("Extra Event 2", "Extra Event 3", "Extra Event 4");

        List<EventShortDto> thirdPage = privateEventService.getEventsByUser(user.getId(), 6, 3);

        assertThat(thirdPage).isEmpty();
    }

    @Test
    void createEventShouldReturnEventFullDtoWhenValidRequest() {
        EventFullDto result = privateEventService.createEvent(user.getId(), newEventDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo(newEventDto.getTitle());
        assertThat(result.getAnnotation()).isEqualTo(newEventDto.getAnnotation());
        assertThat(result.getDescription()).isEqualTo(newEventDto.getDescription());
        assertThat(result.getState()).isEqualTo(EventState.PENDING);

        Event savedEvent = eventRepository.findById(result.getId()).orElseThrow();
        assertThat(savedEvent.getTitle()).isEqualTo(newEventDto.getTitle());
        assertThat(savedEvent.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void createEventShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.createEvent(nonExistentId, newEventDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void createEventShouldThrowConflictExceptionWhenEventDateTooSoon() {
        newEventDto.setEventDate(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> privateEventService.createEvent(user.getId(), newEventDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Event date must be at least 2 hours from now");
    }

    @Test
    void createEventShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;
        newEventDto.setCategory(nonExistentId);

        assertThatThrownBy(() -> privateEventService.createEvent(user.getId(), newEventDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventByIdShouldReturnEventWhenExists() {
        EventFullDto result = privateEventService.getEventById(user.getId(), event.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getTitle()).isEqualTo(event.getTitle());
        assertThat(result.getState()).isEqualTo(event.getState());
    }

    @Test
    void getEventByIdShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.getEventById(nonExistentId, event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventByIdShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.getEventById(user.getId(), nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventByIdShouldThrowNotFoundExceptionWhenUserNotInitiator() {
        assertThatThrownBy(() -> privateEventService.getEventById(anotherUser.getId(), event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + event.getId() + " was not found for user " + anotherUser.getId());
    }

    @Test
    void updateEventShouldUpdateWhenCancelReview() {
        updateRequest.setStateAction(UserStateAction.CANCEL_REVIEW);

        EventFullDto result = privateEventService.updateEvent(user.getId(), event.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(result.getAnnotation()).isEqualTo(updateRequest.getAnnotation());
        assertThat(result.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(result.getState()).isEqualTo(EventState.CANCELED);

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(updatedEvent.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEventShouldUpdateWhenSendToReview() {
        EventFullDto result = privateEventService.updateEvent(user.getId(), event.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(result.getAnnotation()).isEqualTo(updateRequest.getAnnotation());
        assertThat(result.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(result.getState()).isEqualTo(EventState.PENDING);

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(updatedEvent.getState()).isEqualTo(EventState.PENDING);
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.updateEvent(nonExistentId, event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateEventService.updateEvent(user.getId(), nonExistentId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenUserNotInitiator() {
        assertThatThrownBy(() -> privateEventService.updateEvent(anotherUser.getId(), event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + event.getId() + " was not found for user " + anotherUser.getId());
    }

    @Test
    void updateEventShouldThrowConflictExceptionWhenEventPublished() {
        event.setState(EventState.PUBLISHED);
        event = eventRepository.save(event);

        assertThatThrownBy(() -> privateEventService.updateEvent(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Only pending or canceled events can be changed");
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;
        updateRequest.setCategory(nonExistentId);

        assertThatThrownBy(() -> privateEventService.updateEvent(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateEventShouldThrowConflictExceptionWhenEventDateTooSoon() {
        updateRequest.setEventDate(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> privateEventService.updateEvent(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Event date must be at least 2 hours from now");
    }

    @Test
    void updateEventShouldUpdateEventWhenPartialUpdateRequest() {
        updateRequest.setAnnotation(null);
        updateRequest.setDescription(null);

        EventFullDto result = privateEventService.updateEvent(user.getId(), event.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(result.getAnnotation()).isEqualTo(event.getAnnotation());
        assertThat(result.getDescription()).isEqualTo(event.getDescription());
        assertThat(result.getPaid()).isEqualTo(updateRequest.getPaid());
        assertThat(result.getParticipantLimit()).isEqualTo(updateRequest.getParticipantLimit());
        assertThat(result.getRequestModeration()).isEqualTo(updateRequest.getRequestModeration());

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(updatedEvent.getAnnotation()).isEqualTo(event.getAnnotation());
        assertThat(updatedEvent.getState()).isEqualTo(EventState.PENDING);
    }
}