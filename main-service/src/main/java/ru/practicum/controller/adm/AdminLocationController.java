package ru.practicum.controller.adm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;
import ru.practicum.service.adm.AdminLocationService;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
@Slf4j
public class AdminLocationController {

    private final AdminLocationService adminLocationService;

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