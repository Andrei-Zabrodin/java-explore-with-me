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
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.model.SortByType;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
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
    private UserRepository userRepository;

    @MockBean
    private HttpServletRequest httpServletRequest;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private Category category1;
    private Category category2;
    private Event publishedEvent1;
    private Event publishedEvent2;
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

        publishedEvent1 = new Event();
        publishedEvent1.setTitle("Published Event");
        publishedEvent1.setAnnotation("Published Annotation");
        publishedEvent1.setDescription("Published Description");
        publishedEvent1.setCategory(category1);
        publishedEvent1.setInitiator(user);
        publishedEvent1.setEventDate(LocalDateTime.now().plusDays(5));
        publishedEvent1.setCreatedOn(LocalDateTime.now());
        publishedEvent1.setPublishedOn(LocalDateTime.now());
        publishedEvent1.setState(EventState.PUBLISHED);
        publishedEvent1.setLat(55.75);
        publishedEvent1.setLon(37.62);
        publishedEvent1.setPaid(false);
        publishedEvent1.setParticipantLimit(10);
        publishedEvent1.setRequestModeration(true);
        publishedEvent1 = eventRepository.save(publishedEvent1);

        publishedEvent2 = new Event();
        publishedEvent2.setTitle("Another Published Event");
        publishedEvent2.setAnnotation("Another Published Annotation");
        publishedEvent2.setDescription("Another Published Description");
        publishedEvent2.setCategory(category2);
        publishedEvent2.setInitiator(user);
        publishedEvent2.setEventDate(LocalDateTime.now().plusDays(10));
        publishedEvent2.setCreatedOn(LocalDateTime.now());
        publishedEvent2.setPublishedOn(LocalDateTime.now());
        publishedEvent2.setState(EventState.PUBLISHED);
        publishedEvent2.setLat(55.75);
        publishedEvent2.setLon(37.62);
        publishedEvent2.setPaid(true);
        publishedEvent2.setParticipantLimit(10);
        publishedEvent2.setRequestModeration(true);
        publishedEvent2 = eventRepository.save(publishedEvent2);

        pendingEvent = new Event();
        pendingEvent.setTitle("Pending Event");
        pendingEvent.setAnnotation("Pending Annotation");
        pendingEvent.setDescription("Pending Description");
        pendingEvent.setCategory(category1);
        pendingEvent.setInitiator(user);
        pendingEvent.setEventDate(LocalDateTime.now().plusDays(7));
        pendingEvent.setCreatedOn(LocalDateTime.now());
        pendingEvent.setState(EventState.PENDING);
        pendingEvent.setLat(55.75);
        pendingEvent.setLon(37.62);
        pendingEvent.setPaid(false);
        pendingEvent.setParticipantLimit(10);
        pendingEvent.setRequestModeration(true);
        pendingEvent = eventRepository.save(pendingEvent);
    }

    @Test
    void getEventsShouldReturnOnlyPublishedEvents() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, 0, 10, httpServletRequest);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent1.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.get(1).getId()).isEqualTo(publishedEvent2.getId());
        assertThat(result.get(1).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldFilterByText() {
        List<EventShortDto> result = publicEventService.getEvents(
                "Another", null, null, null, null, false,
                null, 0, 10, httpServletRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent2.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldFilterByCategory() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, List.of(category1.getId()), null, null, null, false,
                null, 0, 10, httpServletRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(publishedEvent1.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());
    }

    @Test
    void getEventsShouldFilterByPaid() {
        List<EventShortDto> result = publicEventService.getEvents(
                null, null, true, null, null, false,
                null, 0, 10, httpServletRequest);

        assertThat(result).hasSize(1);
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
        earlierEvent.setInitiator(user);
        earlierEvent.setEventDate(LocalDateTime.now().plusDays(1));
        earlierEvent.setCreatedOn(LocalDateTime.now());
        earlierEvent.setPublishedOn(LocalDateTime.now());
        earlierEvent.setState(EventState.PUBLISHED);
        earlierEvent.setLat(55.75);
        earlierEvent.setLon(37.62);
        earlierEvent.setPaid(false);
        earlierEvent.setParticipantLimit(10);
        earlierEvent.setRequestModeration(true);
        eventRepository.save(earlierEvent);

        List<EventShortDto> result = publicEventService.getEvents(
                null, null, null, null, null, false,
                SortByType.EVENT_DATE, 0, 10, httpServletRequest);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo(earlierEvent.getTitle());
        assertThat(result.get(1).getTitle()).isEqualTo(publishedEvent1.getTitle());
        assertThat(result.get(2).getTitle()).isEqualTo(publishedEvent2.getTitle());
    }

    @Test
    void getEventsShouldReturnEmptyListWhenNoEventsFound() {
        List<EventShortDto> result = publicEventService.getEvents(
                "Nonexistent Event", null, null, null, null, false,
                null, 0, 10, httpServletRequest);

        assertThat(result).isEmpty();
    }

    @Test
    void getEventsShouldReturnEventsWithPagination() {
        List<EventShortDto> firstPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, 0, 1, httpServletRequest);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).getTitle()).isEqualTo(publishedEvent1.getTitle());

        List<EventShortDto> secondPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, 1, 1, httpServletRequest);

        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTitle()).isEqualTo(publishedEvent2.getTitle());

        List<EventShortDto> thirdPage = publicEventService.getEvents(
                null, null, null, null, null, false,
                null, 2, 1, httpServletRequest);

        assertThat(thirdPage).isEmpty();
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
}