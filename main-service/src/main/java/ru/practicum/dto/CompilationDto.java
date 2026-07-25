package ru.practicum.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompilationDto {
    private List<EventShortDto> events;

    private Long id;

    private Boolean pinned;

    private String title;
}
