package ru.practicum.service.adm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.model.Location;
import ru.practicum.model.event.AdminStateAction;
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
class AdminEventServiceIntegrationTest {

    @Autowired
    private AdminEventService adminEventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StatsClient statsClient;

    private Category category;
    private User initiator;
    private Event event;
    private UpdateEventAdminRequest updateRequest;
    private Location location;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        category = new Category();
        category.setName("Category 1");
        category = categoryRepository.save(category);

        initiator = new User();
        initiator.setEmail("initiator@example.com");
        initiator.setName("Initiator");
        initiator = userRepository.save(initiator);

        location = new Location();
        location.setLat(55.75);
        location.setLon(37.62);

        event = new Event();
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setInitiator(initiator);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLat(55.754167);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event = eventRepository.save(event);

        updateRequest = new UpdateEventAdminRequest();
        updateRequest.setTitle("Updated Event");
        updateRequest.setAnnotation("Updated Annotation");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategory(category.getId());
        updateRequest.setEventDate(LocalDateTime.now().plusDays(7));
        updateRequest.setLocation(location);
        updateRequest.setPaid(true);
        updateRequest.setParticipantLimit(20);
        updateRequest.setRequestModeration(false);
        updateRequest.setStateAction(AdminStateAction.PUBLISH_EVENT);
    }

    @Test
    void getEventsShouldReturnListWhenFiltersApplied() {
        List<EventFullDto> result = adminEventService.getEvents(
                List.of(initiator.getId()),
                List.of(EventState.PENDING.name()),
                List.of(category.getId()),
                null,
                null,
                0,
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(event.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(event.getTitle());
        assertThat(result.get(0).getState()).isEqualTo(event.getState());
    }

    @Test
    void getEventsShouldReturnEmptyListWhenNoEventsFound() {
        List<EventFullDto> result = adminEventService.getEvents(
                List.of(999L),
                List.of(EventState.PUBLISHED.name()),
                List.of(999L),
                null,
                null,
                0,
                10
        );

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsShouldReturnEventsWithPagination() {
        for (int i = 0; i < 5; i++) {
            Event extraEvent = new Event();
            extraEvent.setTitle("Extra Event " + i);
            extraEvent.setAnnotation("Annotation " + i);
            extraEvent.setDescription("Description " + i);
            extraEvent.setCategory(category);
            extraEvent.setConfirmedRequests(0L);
            extraEvent.setInitiator(initiator);
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

        List<EventFullDto> firstPage = adminEventService.getEvents(
                null, null, null, null, null, 0, 3
        );

        assertThat(firstPage).hasSize(3);
        assertThat(firstPage).extracting(EventFullDto::getTitle)
                .containsExactly(event.getTitle(), "Extra Event 0", "Extra Event 1");

        List<EventFullDto> secondPage = adminEventService.getEvents(
                null, null, null, null, null, 3, 3
        );

        assertThat(secondPage).hasSize(3);
        assertThat(secondPage).extracting(EventFullDto::getTitle)
                .containsExactly("Extra Event 2", "Extra Event 3", "Extra Event 4");

        List<EventFullDto> thirdPage = adminEventService.getEvents(
                null, null, null, null, null, 6, 3
        );

        assertThat(thirdPage).isEmpty();
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenInvalidState() {
        assertThatThrownBy(() -> adminEventService.getEvents(
                null,
                List.of("INVALID_STATE"),
                null,
                null,
                null,
                0,
                10
        )).isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state");
    }

    @Test
    void updateEventShouldUpdateEventWhenValidRequest() {
        EventFullDto result = adminEventService.updateEvent(event.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(result.getAnnotation()).isEqualTo(updateRequest.getAnnotation());
        assertThat(result.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(result.getPaid()).isEqualTo(updateRequest.getPaid());
        assertThat(result.getParticipantLimit()).isEqualTo(updateRequest.getParticipantLimit());
        assertThat(result.getRequestModeration()).isEqualTo(updateRequest.getRequestModeration());

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(updatedEvent.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(updatedEvent.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEventShouldUpdateEventWhenPartialUpdateRequest() {
        updateRequest.setAnnotation(null);
        updateRequest.setDescription(null);

        EventFullDto result = adminEventService.updateEvent(event.getId(), updateRequest);

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
        assertThat(updatedEvent.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(updatedEvent.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEventShouldThrowConflictExceptionWhenPublishingNonPendingEvent() {
        event.setState(EventState.PUBLISHED);
        event = eventRepository.save(event);

        assertThatThrownBy(() -> adminEventService.updateEvent(event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot publish the event because it's not in the right state");
    }

    @Test
    void updateEventShouldThrowConflictExceptionWhenEventDateIsInvalid() {
        event.setEventDate(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> adminEventService.updateEvent(event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Event date must be at least one hour from now");
    }

    @Test
    void updateEventShouldRejectEventWhenStateActionIsReject() {
        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);

        EventFullDto result = adminEventService.updateEvent(event.getId(), updateRequest);

        assertThat(result.getState()).isEqualTo(EventState.CANCELED);

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEventShouldThrowConflictExceptionWhenRejectingPublishedEvent() {
        event.setState(EventState.PUBLISHED);
        event = eventRepository.save(event);

        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);

        assertThatThrownBy(() -> adminEventService.updateEvent(event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot reject the event because it's not in the right state");
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminEventService.updateEvent(nonExistentId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateEventShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        Long nonExistentId = 999L;
        updateRequest.setCategory(nonExistentId);

        assertThatThrownBy(() -> adminEventService.updateEvent(event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateEventShouldThrowConflictExceptionPublishedEventDateIsChangedToInvalidDate() {
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        event = eventRepository.save(event);

        updateRequest.setStateAction(null);
        updateRequest.setEventDate(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() -> adminEventService.updateEvent(event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Event date must be at least one hour after the publication date");
    }
}