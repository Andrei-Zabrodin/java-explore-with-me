package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.Location;
import ru.practicum.model.event.UserStateAction;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Event annotation must be between 20 and 2000 characters")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Event description must be between 20 and 7000 characters")
    private String description;

    @Future(message = "Event date should be later then current date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private UserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Event title must be between 3 and 120 characters")
    private String title;
}
