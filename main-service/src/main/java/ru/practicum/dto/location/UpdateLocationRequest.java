package ru.practicum.dto.location;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLocationRequest {

    @DecimalMin(value = "-90.0", message = "Location latitude must be in range from -90° to 90°")
    @DecimalMax(value = "90.0", message = "Location latitude must be in range from -90° to 90°")
    private Double lat;

    @DecimalMin(value = "-180.0", message = "Location longitude must be in range from -180° to 180°")
    @DecimalMax(value = "180.0", message = "Location longitude must be in range from -180° to 180°")
    private Double lon;

    @Size(max = 120, message = "Location name must be less than 120 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;
}
