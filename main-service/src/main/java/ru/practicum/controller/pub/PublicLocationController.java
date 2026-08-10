package ru.practicum.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.service.pub.PublicLocationService;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicLocationController {

    private final PublicLocationService publicLocationService;

    @GetMapping
    public List<LocationDto> getLocations(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "10.0") @PositiveOrZero Double radius,
            @RequestParam(defaultValue = "OFFICIAL") String locationState,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("GET /locations - получение локаций с параметрами: text={}, lat={}, lon={}, radius={}," +
                        " locationState ={}, from={}, size={}", text, lat, lon, radius, locationState, from, size);

        return publicLocationService.getLocations(text, lat, lon, radius, locationState, from, size);
    }

    @GetMapping("/{id}")
    public LocationDto getLocationById(@PathVariable Long id,
                                       @RequestParam(defaultValue = "OFFICIAL") String locationState) {
        log.info("GET /locations/{} - получение локации", id);
        return publicLocationService.getLocationById(id, locationState);
    }
}