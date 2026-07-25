package ru.practicum.dto;

import ru.practicum.model.Location;

import java.time.LocalDateTime;

public class NewEventDto {

    private Long id;

    private String annotation;

    private Long category;

    private String description;

    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid = false;

    private Integer participantLimit = 0;

    private Boolean requestModeration = false;

    private String title;
}
