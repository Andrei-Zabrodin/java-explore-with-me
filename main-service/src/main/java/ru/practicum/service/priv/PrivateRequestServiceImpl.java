package ru.practicum.service.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.RequestState;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestServiceImpl implements PrivateRequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        // Проверяем, что пользователь существует
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        log.debug("Found {} requests for user {}", requests.size(), userId);

        return requests.stream()
                .map(requestMapper::convertToDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        // Проверяем, что пользователь существует
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Проверяем, что событие существует
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Проверяем, что пользователь является инициатором события
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the owner of event with id=" + eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        log.debug("Found {} requests for event {}", requests.size(), eventId);

        return requests.stream()
                .map(requestMapper::convertToDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        // Проверяем, что пользователь существует
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Проверяем, что событие существует
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Проверяем, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        // Проверяем, что пользователь не является инициатором события
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate in his own event");
        }

        // Проверяем, что лимит участников не превышен
        if (event.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestState.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit has been reached");
            }
        }

        // Проверяем, что пользователь ещё не подавал заявку
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Participation request for this event has already been created");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        // Eсли модерация отключена, сразу подтверждаем заявку
        if (Boolean.FALSE.equals(event.getRequestModeration())) {
            request.setStatus(RequestState.CONFIRMED);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("User {} created request for event {}", userId, eventId);

        return requestMapper.convertToDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        // Проверяем, что пользователь существует
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Находим запрос и проверяем, что он принадлежит пользователю
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found for user " + userId));

        request.setStatus(RequestState.CANCELED);

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("User {} canceled request with id: {}", userId, requestId);

        return requestMapper.convertToDto(savedRequest);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        // Проверяем, что пользователь существует
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Проверяем, что событие существует
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Проверяем, что пользователь является инициатором события
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the owner of event with id=" + eventId);
        }

        // Если лимит 0 или модерация отключена — подтверждение не требуется
        if (event.getParticipantLimit() == 0 || Boolean.FALSE.equals(event.getRequestModeration())) {
            throw new ConflictException("Event does not require request confirmation");
        }

        // Проверяем, что все заявки существуют и имеют статус PENDING
        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());
        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("Some requests were not found");
        }
        for (ParticipationRequest req : requests) {
            if (!req.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request does not belong to this event");
            }
            if (req.getStatus() != RequestState.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        // Считаем текущее количество подтверждённых заявок
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestState.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - confirmedCount;

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        // Обрабатываем заявки в зависимости от статуса
        if (request.getStatus() == RequestState.CONFIRMED) {
            // Проверяем, что есть свободные места
            if (availableSlots <= 0) {
                throw new ConflictException("Participant limit has been reached");
            }

            // Подтверждаем заявки до лимита
            for (ParticipationRequest req : requests) {
                if (availableSlots > 0) {
                    req.setStatus(RequestState.CONFIRMED);
                    confirmedRequests.add(req);
                    availableSlots--;
                } else {
                    // Если лимит исчерпан — остальные отклоняем
                    req.setStatus(RequestState.REJECTED);
                    rejectedRequests.add(req);
                }
            }

            // Если после подтверждения лимит исчерпан — отклоняем все остальные PENDING заявки
            if (availableSlots <= 0) {
                List<ParticipationRequest> pending = requestRepository
                        .findAllByEventIdAndStatus(eventId, RequestState.PENDING);
                for (ParticipationRequest req : pending) {
                    if (!request.getRequestIds().contains(req.getId())) {
                        req.setStatus(RequestState.REJECTED);
                        rejectedRequests.add(req);
                    }
                }
            }

        } else if (request.getStatus() == RequestState.REJECTED) {
            // Отклоняем все указанные заявки
            for (ParticipationRequest req : requests) {
                req.setStatus(RequestState.REJECTED);
                rejectedRequests.add(req);
            }
        }

        // Сохраняем все изменения в заявках
        List<ParticipationRequest> updatedRequests = new ArrayList<>();
        updatedRequests.addAll(confirmedRequests);
        updatedRequests.addAll(rejectedRequests);
        requestRepository.saveAll(updatedRequests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests.stream()
                .map(requestMapper::convertToDto)
                .toList());
        result.setRejectedRequests(rejectedRequests.stream()
                .map(requestMapper::convertToDto)
                .toList());
        return result;
    }
}
