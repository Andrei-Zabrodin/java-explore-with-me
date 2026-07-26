package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.Location;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewEventDto {

    @NotBlank(message = "Event annotation must not be blank")
    @Size(min = 20, max = 2000, message = "Event annotation must be between 20 and 2000 characters")
    private String annotation;

    @NotNull(message = "Category must not be null")
    private Long category;

    @NotBlank(message = "Event description must not be blank")
    @Size(min = 20, max = 7000, message = "Event description must be between 20 and 7000 characters")
    private String description;

    @NotNull(message = "Event date must not be null")
    @Future(message = "Event date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Location must not be null")
    private Location location;

    private Boolean paid = false;

    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotBlank(message = "Event title must not be blank")
    @Size(min = 3, max = 120, message = "Event title must be between 3 and 120 characters")
    private String title;
}
