package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.priv.PrivateRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Slf4j
public class PrivateRequestController {
    private final PrivateRequestService privateRequestService;

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable Long userId) {
        log.info("GET /users/{}/requests - получение заявок пользователя", userId);
        return privateRequestService.getRequestsByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("POST /users/{}/requests?eventId={} - создание заявки на участие", userId, eventId);
        return privateRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - отмена заявки пользователем", userId, requestId);
        return privateRequestService.cancelRequest(userId, requestId);
    }
}
