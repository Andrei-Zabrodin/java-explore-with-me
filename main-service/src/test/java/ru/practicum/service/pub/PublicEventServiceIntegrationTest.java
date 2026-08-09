package ru.practicum.service.pub;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.model.SortByType;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PublicEventServiceIntegrationTest {

    @Autowired
    private PublicEventService publicEventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private HttpServletRequest httpServletRequest;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private Category category1;
    private Category category2;
    private Location location1;
    private Location location2;
    private Event publishedEvent1;
    private Event publishedEvent2;
    private Event publishedEvent3;
    private Event pendingEvent;

    @BeforeEach
    void setUp() {
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getRequestURI()).thenReturn("/events");

        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of());
        doNothing().when(statsClient).sendHit(any(EndpointHitDto.class));

        user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user = userRepository.save(user);

        category1 = new Category();
        category1.setName("Category 1");
        category1 = categoryRepository.save(category1);

        category2 = new Category();
        category2.setName("Category 2");
        category2 = categoryRepository.save(category2);

        location1 = new Location();
        location1.setLat(55.75);
        location1.setLon(37.62);
        location1.setName("Location 1");
        location1.setStatus(LocationStatus.OFFICIAL);
        location1 = locationRepository.save(location1);

        location2 = new Location();
        location2.setLat(56.75);
        location2.setLon(38.62);
        location2.setName("Location 2");
        location2.setStatus(LocationStatus.OFFICIAL);
        location2 = locationRepository.save(location2);

        publishedEvent1 = new Event();
        publishedEvent1.setTitle("Published Event");
        publishedEvent1.setAnnotation("Published Annotation");
        publishedEvent1.setDescription("Published Description");
        publishedEvent1.setCategory(category1);
        publishedEvent1.setConfirmedRequests(0L);
        publishedEvent1.setInitiator(user);
        publishedEvent1.setEventDate(LocalDateTime.now().plusDays(5));
        publishedEvent1.setCreatedOn(LocalDateTime.now());
        publishedEvent1.setPublishedOn(LocalDateTime.now());
        publishedEvent1.setState(EventState.PUBLISHED);
        publishedEvent1.setLocation(location1);
        publishedEvent1.setPaid(false);
        publishedEvent1.setParticipantLimit(10);
        publishedEvent1.setRequestModeration(true);
        publishedEvent1 = eventRepository.save(publishedEvent1);

        publishedEvent2 = new Event();
        publishedEvent2.setTitle("Another Published Event");
        publishedEvent2.setAnnotation("Another Published Annotation");
        publishedEvent2.setDescription("Another Published Description");
        publishedEvent2.setCategory(category2);
        publishedEvent2.setConfirmedRequests(0L);
        publishedEvent2.setInitiator(user);
        publishedEvent2.setEventDate(LocalDateTime.now().plusDays(10));
        publishedEvent2.setCreatedOn(LocalDateTime.now());
        publishedEvent2.setPublishedOn(LocalDateTime.now());
        publishedEvent2.setState(EventState.PUBLISHED);
        publishedEvent2.setLocation(location2);
        publishedEvent2.setPaid(true);
        publishedEvent2.setParticipantLimit(10);
        publishedEvent2.setRequestModeration(true);
        publishedEvent2 = eventRepository.save(publishedEvent2);

        publishedEvent3 = new Event();
        publishedEvent3.setTitle("Another Another Published Event");
        publishedEvent3.setAnnotation("Another Another Published Annotation");
        publishedEvent3.setDescription("Another Another Published Description");
        publishedEvent3.setCategory(category2);
        publishedEvent3.setConfirmedRequests(10L);
        publishedEvent3.setInitiator(user);
        publishedEvent3.setEventDate(LocalDateTime.now().plusDays(20));
        publishedEvent3.setCreatedOn(LocalDateTime.now());
        publishedEvent3.setPublishedOn(LocalDateTime.now());
        publishedEvent3.setState(EventState.PUBLISHED);
        publishedEvent3.setLocation(location1);
        publishedEvent3.setPaid(true);
        publishedEvent3.setParticipantLimit(10);
        publishedEvent3.setRequestModeration(true);
        publishedEvent3 = eventRepository.save(publishedEvent3);

        pendingEvent = new Event();
        pendingEvent.setTitle("Pending Event");
        pendingEvent.setAnnotation("Pending Annotation");
        pendingEvent.setDescription("Pending Description");
        pendingEvent.setCategory(category1);
        pendingEvent.setConfirmedRequests(0L);
        pendingEvent.setInitiator(user);
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(7));
        pendingEvent.setCreatedOn(LocalDateTime.now());
        pendingEvent.setState(EventState.PENDING);
        pendingEvent.setLocation(location1);
        pendingEvent.setPaid(false);
        pendingEvent.setParticipantLimit(10);
        pendingEvent.setRequestModeration(true);
        pendingEvent = eventRepository.save(pendingEvent);
    }

    @Test
    void getEventsShouldReturnOnlyPublishedEvents() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent1.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.get(1).getId()).isEqualTo(publishedEvent2.getId());
        assertThat(result.get(1).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldFilterByText() {
        List<EventShortDto> result = publicEventService.getEvents(
                "Another", null, null, null, null, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent2.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldFilterByCategory() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, List.of(category1.getId()), null, null, null, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent1.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());
    }

    @Test
    void getEventsShouldFilterByPaid() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, null, true, null, null, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent2.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldSortByEventDate() {
        Event earlierEvent = new Event();
        earlierEvent.setTitle("Earlier Event");
        earlierEvent.setAnnotation("Earlier Annotation");
        earlierEvent.setDescription("Earlier Description");
        earlierEvent.setCategory(category1);
        earlierEvent.setConfirmedRequests(0L);
        earlierEvent.setInitiator(user);
        earlierEvent.setEventDate(LocalDateTime.now().plusDays(1));
        earlierEvent.setCreatedOn(LocalDateTime.now());
        earlierEvent.setPublishedOn(LocalDateTime.now());
        earlierEvent.setState(EventState.PUBLISHED);
        earlierEvent.setLocation(location1);
        earlierEvent.setPaid(false);
        earlierEvent.setParticipantLimit(10);
        earlierEvent.setRequestModeration(true);
        eventRepository.save(earlierEvent);

        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, null, null, false,
                SortByType.EVENT_DATE, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getTitle()).isEqualTo(earlierEvent.getTitle());
        assertThat(result.get(1).getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.get(2).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldReturnEmptyListWhenNoEventsFound() {
        List<EventShortDto> result = publicEventService.getEvents(
                "Nonexistent Event", null, null, null, null, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsShouldReturnEventsWithPagination() {
        List<EventShortDto> firstPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, null, null, 10.0, 0, 1, httpServletRequest);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());

        List<EventShortDto> secondPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, null, null, 10.0, 1, 1, httpServletRequest);

        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTitle()).isEqualTo(publishedEvent2.getTitle());

        List<EventShortDto> thirdPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, null, null, 10.0, 2, 1, httpServletRequest);

        assertThat(thirdPage.get(0).getTitle()).isEqualTo(publishedEvent3.getTitle());
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenRangeStartAfterRangeEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> publicEventService.getEvents(
                null, null, null, start, end, false,
                null, null, null, 10.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата начала диапазона выгрузки должна быть не позже даты конца");
    }

    @Test
    void getEventsShouldReturnEventsWhenRangeStartEqualsRangeEnd() {
        LocalDateTime date = LocalDateTime.now().plusDays(5);

        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, date, date, false,
                null, null, null, 10.0, 0, 10, httpServletRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenOnlyLatProvided() {
        assertThatThrownBy(() -> publicEventService.getEvents(null, null, null, null, null,
                false, null, 55.75, null, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenOnlyLonProvided() {
        assertThatThrownBy(() -> publicEventService.getEvents(null, null, null, null, null,
                null, null, null, 37.61, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenLatBelowMin() {
        assertThatThrownBy(() -> publicEventService.getEvents(null, null, null, null, null,
                false, null, -90.1, 37.61, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude must be in range from -90° to 90°");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenLatAboveMax() {
        assertThatThrownBy(() -> publicEventService.getEvents(null, null, null, null, null,
                false, null, 90.1, 37.6173, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude must be in range from -90° to 90°");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenLonBelowMin() {
        assertThatThrownBy(() -> publicEventService.getEvents(
                null, null, null, null, null, false, null, 55.75,
                -180.1, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Longitude must be in range from -180° to 180°");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenLonAboveMax() {
        assertThatThrownBy(() -> publicEventService.getEvents(
                null, null, null, null, null, false, null, 55.75,
                180.1, 100.0, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Longitude must be in range from -180° to 180°");
    }

    @Test
    void getEventsShouldThrowValidationExceptionWhenRadiusExceedsLimit() {
        assertThatThrownBy(() -> publicEventService.getEvents(
                null, null, null, null, null, false, null, 55.75,
                37.61, 1000.1, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Radius cannot exceed 1000 km");
    }

    @Test
    void getEventsShouldAcceptNullLatLonAndRadius() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, null, null, false, null, null,
                null, null, 0, 10, httpServletRequest);

        assertThat(result).isNotNull();
    }

    @Test
    void getEventsShouldFilterByCoordinatesAndRadius() {
        List<EventShortDto> result = publicEventService.getEvents(null, null, null, null,
                null, false, null, 55.0, 37.0, 100.0, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle)
                .containsExactlyInAnyOrder(publishedEvent1.getTitle(), publishedEvent3.getTitle());
    }

    @Test
    void getEventsShouldReturnEmptyListWhenCoordinatesOutOfRadius() {
        List<EventShortDto> result = publicEventService.getEvents(null, null, null, null,
                null, null, null, 55.0, 37.0, 10.0, 0, 10, httpServletRequest);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventByIdShouldReturnPublishedEventWhenExists() {
        EventFullDto result = publicEventService.getEventById(publishedEvent1.getId(), httpServletRequest);

        assertThat(result.getId()).isEqualTo(publishedEvent1.getId());
        assertThat(result.getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.getState()).isEqualTo(EventState.PUBLISHED);
    }

    @Test
    void getEventByIdShouldThrowNotFoundExceptionWhenEventNotPublished() {
        assertThatThrownBy(() -> publicEventService.getEventById(pendingEvent.getId(), httpServletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + pendingEvent.getId() + " is not yet published");
    }

    @Test
    void getEventByIdShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> publicEventService.getEventById(nonExistentId, httpServletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventsByLocationIdShouldReturnEventsForLocation() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null, null,
                null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle)
                .contains(publishedEvent1.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle)
                .doesNotContain(publishedEvent2.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldThrowNotFoundExceptionWhenLocationNotFound() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> publicEventService.getEventsByLocationId(nonExistentId, null, null, null,
                null, null, false, null, 0, 10, httpServletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location with id=" + nonExistentId + " was not found");
    }

    @Test
    void getEventsByLocationIdShouldReturnOnlyPublishedEvents() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle).contains(publishedEvent1.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle).doesNotContain(pendingEvent.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldReturnEmptyListWhenTextNotFound() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), "NonExistentText",
                null, null, null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsByLocationIdShouldFilterByCategories() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, List.of(category1.getId()),
                null, null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle).contains(publishedEvent1.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle).doesNotContain(publishedEvent3.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldFilterByPaid() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                true, null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle).contains(publishedEvent3.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle).doesNotContain(publishedEvent1.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldFilterByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(10);

        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, start, end, false, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle).contains(publishedEvent1.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle).doesNotContain(publishedEvent3.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldThrowValidationExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, start, end, false, null, 0, 10, httpServletRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Start date must be before end date");
    }

    @Test
    void getEventsByLocationIdShouldFilterByOnlyAvailable() {
        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, null, null, true, null, 0, 10, httpServletRequest);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(EventShortDto::getTitle).contains(publishedEvent1.getTitle());
        assertThat(result).extracting(EventShortDto::getTitle).doesNotContain(publishedEvent3.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldSortByEventDate() {
        Event earlierEvent = new Event();
        earlierEvent.setTitle("Earlier Event");
        earlierEvent.setAnnotation("Earlier Annotation");
        earlierEvent.setDescription("Earlier Description");
        earlierEvent.setCategory(category1);
        earlierEvent.setConfirmedRequests(0L);
        earlierEvent.setInitiator(user);
        earlierEvent.setEventDate(LocalDateTime.now().plusDays(1));
        earlierEvent.setCreatedOn(LocalDateTime.now());
        earlierEvent.setPublishedOn(LocalDateTime.now());
        earlierEvent.setState(EventState.PUBLISHED);
        earlierEvent.setLocation(location1);
        earlierEvent.setPaid(false);
        earlierEvent.setParticipantLimit(10);
        earlierEvent.setRequestModeration(true);
        eventRepository.save(earlierEvent);

        List<EventShortDto> result = publicEventService.getEventsByLocationId(location1.getId(), null, null, null, null,
                null, false, SortByType.EVENT_DATE, 0, 10, httpServletRequest);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo(earlierEvent.getTitle());
        assertThat(result.get(1).getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.get(2).getTitle()).isEqualTo(publishedEvent3.getTitle());
    }

    @Test
    void getEventsByLocationIdShouldSupportPagination() {
        for (int i = 0; i < 4; i++) {
            Event event = new Event();
            event.setTitle("Pagination Event " + i);
            event.setDescription("Description");
            event.setAnnotation("Annotation");
            event.setEventDate(LocalDateTime.now().plusDays(1 + i));
            event.setState(EventState.PUBLISHED);
            event.setCreatedOn(LocalDateTime.now());
            event.setPublishedOn(LocalDateTime.now());
            event.setCategory(category1);
            event.setLocation(location1);
            event.setPaid(false);
            event.setParticipantLimit(0);
            event.setRequestModeration(true);
            event.setConfirmedRequests(0L);
            event.setInitiator(user);
            eventRepository.save(event);
        }

        List<EventShortDto> firstPage = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, null, null, false, null, 0, 3, httpServletRequest);
        List<EventShortDto> secondPage = publicEventService.getEventsByLocationId(location1.getId(), null, null,
                null, null, null, false, null, 3, 3, httpServletRequest);

        assertThat(firstPage).extracting(EventShortDto::getTitle)
                .containsExactlyInAnyOrder(publishedEvent1.getTitle(), publishedEvent3.getTitle(), "Pagination Event 0");
        assertThat(secondPage).extracting(EventShortDto::getTitle)
                .containsExactlyInAnyOrder("Pagination Event 1", "Pagination Event 2", "Pagination Event 3");
    }

    @Test
    void getEventsByLocationIdShouldReturnEmptyListWhenLocationHasNoEvents() {
        Location emptyLocation = new Location();
        emptyLocation.setLat(59.93);
        emptyLocation.setLon(30.33);
        emptyLocation.setName("Empty Location");
        emptyLocation.setStatus(LocationStatus.OFFICIAL);
        emptyLocation = locationRepository.save(emptyLocation);

        List<EventShortDto> result = publicEventService.getEventsByLocationId(emptyLocation.getId(), null, null,
                null, null, null, false, null, 0, 10, httpServletRequest);

        assertThat(result).isEmpty();
    }
}