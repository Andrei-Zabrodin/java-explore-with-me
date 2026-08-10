package ru.practicum.service.adm;

import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;

public interface AdminLocationService {

    LocationDto createLocation(NewLocationDto dto);

    LocationDto updateLocation(Long id, UpdateLocationRequest request);

    void deleteLocation(Long id);
}
