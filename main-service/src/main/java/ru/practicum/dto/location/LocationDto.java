package ru.practicum.dto.location;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.location.LocationStatus;

@Getter
@Setter
public class LocationDto {
    private Long id;
    private Double lat;
    private Double lon;
    private String name;
    private String description;
    private String address;
    private LocationStatus status;
}
