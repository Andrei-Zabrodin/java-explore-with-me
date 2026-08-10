package ru.practicum.service.pub;

import ru.practicum.dto.location.LocationDto;

import java.util.List;

public interface PublicLocationService {

    List<LocationDto> getLocations(String text, Double lat, Double lon, Double radius, String locationState,
                                   int from, int size);

    LocationDto getLocationById(Long id, String locationState);
}
