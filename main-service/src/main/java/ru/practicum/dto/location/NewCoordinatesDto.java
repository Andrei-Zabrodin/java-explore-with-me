package ru.practicum.dto.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCoordinatesDto {

    @NotNull(message = "Location latitude must not be null")
    @DecimalMin(value = "-90.0", message = "Location latitude must be in range from -90° to 90°")
    @DecimalMax(value = "90.0", message = "Location latitude must be in range from -90° to 90°")
    private Double lat;

    @NotNull(message = "Location longitude must not be null")
    @DecimalMin(value = "-180.0", message = "Location longitude must be in range from -180° to 180°")
    @DecimalMax(value = "180.0", message = "Location longitude must be in range from -180° to 180°")
    private Double lon;
}
