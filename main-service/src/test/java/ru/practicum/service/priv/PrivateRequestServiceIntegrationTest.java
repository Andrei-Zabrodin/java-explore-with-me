package ru.practicum.service.priv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.RequestState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PrivateRequestServiceIntegrationTest {

    @Autowired
    private PrivateRequestService privateRequestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private User anotherUser;
    private User newUser;
    private Category category;
    private Event event;
    private Event newEvent;
    private ParticipationRequest request;
    private ParticipationRequest newRequest;

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

        newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setName("New User");
        newUser = userRepository.save(newUser);

        category = new Category();
        category.setName("Category 1");
        category = categoryRepository.save(category);

        event = new Event();
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event.setState(EventState.PUBLISHED);
        event.setLat(55.75);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event = eventRepository.save(event);


        newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setAnnotation("New Annotation");
        newEvent.setDescription("New Description");
        newEvent.setCategory(category);
        newEvent.setInitiator(user);
        newEvent.setEventDate(LocalDateTime.now().plusDays(10));
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setPublishedOn(LocalDateTime.now());
        newEvent.setState(EventState.PUBLISHED);
        newEvent.setLat(55.75);
        newEvent.setLon(37.62);
        newEvent.setPaid(false);
        newEvent.setParticipantLimit(10);
        newEvent.setRequestModeration(true);
        newEvent = eventRepository.save(newEvent);

        request = new ParticipationRequest();
        request.setRequester(anotherUser);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());
        request.setStatus(RequestState.PENDING);
        request = requestRepository.save(request);

        newRequest = new ParticipationRequest();
        newRequest.setRequester(anotherUser);
        newRequest.setEvent(event);
        newRequest.setCreated(LocalDateTime.now());
        newRequest.setStatus(RequestState.PENDING);
    }

    @Test
    void getRequestsByUserShouldReturnListWhenUserExists() {
        List<ParticipationRequestDto> result = privateRequestService.getRequestsByUser(anotherUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(request.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(RequestState.PENDING);
    }

    @Test
    void getRequestsByUserShouldReturnEmptyListWhenNoRequests() {
        List<ParticipationRequestDto> result = privateRequestService.getRequestsByUser(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getRequestsByUserShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.getRequestsByUser(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void getRequestsByEventOwnerShouldReturnListWhenOwnerRequests() {
        List<ParticipationRequestDto> result = privateRequestService.getRequestsByEventOwner(user.getId(), event.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(request.getId());
        assertThat(result.get(0).getEvent()).isEqualTo(event.getId());
        assertThat(result.get(0).getRequester()).isEqualTo(anotherUser.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(RequestState.PENDING);
    }

    @Test
    void getRequestsByEventOwnerShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.getRequestsByEventOwner(nonExistentId, event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void getRequestsByEventOwnerShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.getRequestsByEventOwner(user.getId(), nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void getRequestsByEventOwnerShouldThrowNotFoundExceptionWhenUserNotOwner() {
        assertThatThrownBy(() -> privateRequestService.getRequestsByEventOwner(anotherUser.getId(), event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + anotherUser.getId() + " is not the owner of event with id=" + event.getId());
    }

    @Test
    void createRequestShouldReturnCreatedWhenValid() {
        ParticipationRequestDto result = privateRequestService.createRequest(anotherUser.getId(), newEvent.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEvent()).isEqualTo(newEvent.getId());
        assertThat(result.getRequester()).isEqualTo(anotherUser.getId());
        assertThat(result.getStatus()).isEqualTo(RequestState.PENDING);

        ParticipationRequest savedRequest = requestRepository.findById(result.getId()).orElseThrow();
        assertThat(savedRequest.getStatus()).isEqualTo(RequestState.PENDING);
    }

    @Test
    void createRequestShouldAutoConfirmWhenModerationDisabled() {
        newEvent.setRequestModeration(false);
        eventRepository.save(newEvent);

        ParticipationRequestDto result = privateRequestService.createRequest(anotherUser.getId(), newEvent.getId());

        assertThat(result.getStatus()).isEqualTo(RequestState.CONFIRMED);
    }

    @Test
    void createRequestShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.createRequest(nonExistentId, event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void createRequestShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.createRequest(anotherUser.getId(), nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void createRequestShouldThrowConflictExceptionWhenEventNotPublished() {
        newEvent.setState(EventState.PENDING);
        eventRepository.save(newEvent);

        assertThatThrownBy(() -> privateRequestService.createRequest(anotherUser.getId(), newEvent.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot participate in unpublished event");
    }

    @Test
    void createRequestShouldThrowConflictExceptionWhenInitiatorRequests() {
        assertThatThrownBy(() -> privateRequestService.createRequest(user.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Initiator cannot participate in his own event");
    }

    @Test
    void createRequestShouldThrowConflictExceptionWhenParticipantLimitReached() {
        event.setParticipantLimit(1);
        eventRepository.save(event);

        request.setStatus(RequestState.CONFIRMED);
        requestRepository.save(request);

        assertThatThrownBy(() -> privateRequestService.createRequest(newUser.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Participant limit has been reached");
    }

    @Test
    void createRequestShouldThrowConflictExceptionWhenDuplicateRequest() {
        assertThatThrownBy(() -> privateRequestService.createRequest(anotherUser.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Participation request for this event has already been created");
    }

    @Test
    void cancelRequestShouldCancelWhenValid() {
        ParticipationRequestDto result = privateRequestService.cancelRequest(anotherUser.getId(), request.getId());

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getStatus()).isEqualTo(RequestState.CANCELED);

        ParticipationRequest updatedRequest = requestRepository.findById(request.getId()).orElseThrow();
        assertThat(updatedRequest.getStatus()).isEqualTo(RequestState.CANCELED);
    }

    @Test
    void cancelRequestShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.cancelRequest(nonExistentId, request.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void cancelRequestShouldThrowNotFoundExceptionWhenRequestNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> privateRequestService.cancelRequest(anotherUser.getId(), nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Request with id=" + nonExistentId + " was not found for user " + anotherUser.getId());
    }

    @Test
    void updateRequestStatusShouldConfirmRequestsWhenValid() {
        newRequest = requestRepository.save(newRequest);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId(), newRequest.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        EventRequestStatusUpdateResult result = privateRequestService.updateRequestStatus(
                user.getId(), event.getId(), updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(2);
        assertThat(result.getRejectedRequests()).isEmpty();
        assertThat(result.getConfirmedRequests().get(0).getId()).isEqualTo(request.getId());
        assertThat(result.getConfirmedRequests().get(1).getId()).isEqualTo(newRequest.getId());
        assertThat(result.getConfirmedRequests().get(0).getStatus()).isEqualTo(RequestState.CONFIRMED);
        assertThat(result.getConfirmedRequests().get(1).getStatus()).isEqualTo(RequestState.CONFIRMED);
    }

    @Test
    void updateRequestStatusShouldRejectExcessRequestsWhenLimitReached() {
        event.setParticipantLimit(1);
        eventRepository.save(event);

        newRequest = requestRepository.save(newRequest);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId(), newRequest.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        EventRequestStatusUpdateResult result = privateRequestService.updateRequestStatus(
                user.getId(), event.getId(), updateRequest);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).hasSize(1);
        assertThat(result.getConfirmedRequests().get(0).getStatus()).isEqualTo(RequestState.CONFIRMED);
        assertThat(result.getConfirmedRequests().get(0).getId()).isEqualTo(request.getId());
        assertThat(result.getRejectedRequests().get(0).getStatus()).isEqualTo(RequestState.REJECTED);
        assertThat(result.getRejectedRequests().get(0).getId()).isEqualTo(newRequest.getId());
    }

    @Test
    void updateRequestStatusShouldRejectAllWhenStatusRejected() {
        newRequest = requestRepository.save(newRequest);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId(), newRequest.getId()));
        updateRequest.setStatus(RequestState.REJECTED);

        EventRequestStatusUpdateResult result = privateRequestService.updateRequestStatus(
                user.getId(), event.getId(), updateRequest);

        assertThat(result.getConfirmedRequests()).isEmpty();
        assertThat(result.getRejectedRequests()).hasSize(2);
        assertThat(result.getRejectedRequests().get(0).getId()).isEqualTo(request.getId());
        assertThat(result.getRejectedRequests().get(1).getId()).isEqualTo(newRequest.getId());
        assertThat(result.getRejectedRequests().get(0).getStatus()).isEqualTo(RequestState.REJECTED);
        assertThat(result.getRejectedRequests().get(1).getStatus()).isEqualTo(RequestState.REJECTED);
    }

    @Test
    void updateRequestShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(nonExistentId, event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateRequestShouldThrowNotFoundExceptionWhenUserNotOwner() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(anotherUser.getId(), event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + anotherUser.getId() + " is not the owner of event with id=" + event.getId());
    }

    @Test
    void updateRequestShouldThrowNotFoundExceptionWhenEventNotFound() {
        Long nonExistentId = 999L;

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), nonExistentId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Event with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateRequestStatusShouldThrowConflictExceptionWhenLimitZero() {
        event.setParticipantLimit(0);
        eventRepository.save(event);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Event does not require request confirmation");
    }

    @Test
    void updateRequestStatusShouldThrowConflictExceptionWhenModerationDisabled() {
        event.setRequestModeration(false);
        eventRepository.save(event);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Event does not require request confirmation");
    }

    @Test
    void updateRequestStatusShouldThrowConflictExceptionWhenRequestDoesNotBelongToEvent() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.REJECTED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), newEvent.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Request does not belong to this event");
    }

    @Test
    void updateRequestStatusShouldThrowConflictExceptionWhenRequestNotPending() {
        request.setStatus(RequestState.CONFIRMED);
        requestRepository.save(request);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request.getId()));
        updateRequest.setStatus(RequestState.REJECTED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Request must have status PENDING");
    }

    @Test
    void updateRequestStatusShouldThrowNotFoundExceptionWhenRequestNotFound() {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(999L));
        updateRequest.setStatus(RequestState.CONFIRMED);

        assertThatThrownBy(() -> privateRequestService.updateRequestStatus(user.getId(), event.getId(), updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Some requests were not found");
    }
}