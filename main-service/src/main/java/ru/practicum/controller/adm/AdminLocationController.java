package ru.practicum.controller.adm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;
import ru.practicum.service.adm.AdminLocationService;

import java.util.List;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
@Slf4j
public class AdminLocationController {

    private final AdminLocationService adminLocationService;

    @GetMapping
    public List<LocationDto> getLocations(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /admin/locations - поиск локаций с параметрами: from={}, size={}", from, size);
        return adminLocationService.getLocations(from, size);
    }

    @GetMapping("/{locId}")
    public LocationDto getLocationById(@PathVariable Long locId) {
        log.info("GET /admin/locations/{} - получение локации", locId);
        return adminLocationService.getLocationById(locId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto createLocation(@Valid @RequestBody NewLocationDto dto) {
        log.info("POST /admin/locations - создание локации с названием: {}", dto.getName());
        return adminLocationService.createLocation(dto);
    }

    @PatchMapping("/{id}")
    public LocationDto updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request) {
        log.info("PATCH /admin/locations/{} - обновление локации", id);
        return adminLocationService.updateLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
        log.info("DELETE /admin/locations/{} - удаление локации", id);
        adminLocationService.deleteLocation(id);
    }
}