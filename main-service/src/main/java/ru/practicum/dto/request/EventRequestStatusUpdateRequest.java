package ru.practicum.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.request.RequestState;

import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Request ids must not be empty")
    private List<Long> requestIds;

    @NotNull(message = "Status must not be null")
    private RequestState status;
}
