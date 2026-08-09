package ru.practicum.service.adm;

import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;

import java.util.List;

public interface AdminLocationService {

    List<LocationDto> getLocations(int from, int size);

    LocationDto getLocationById(Long locId);

    LocationDto createLocation(NewLocationDto dto);

    LocationDto updateLocation(Long id, UpdateLocationRequest request);

    void deleteLocation(Long id);
}
