package ru.practicum.service.pub;

import ru.practicum.dto.location.LocationDto;

import java.util.List;

public interface PublicLocationService {

    List<LocationDto> getOfficialLocations(String text, Double lat, Double lon, Double radius, int from, int size);

    LocationDto getOfficialLocationById(Long id);
}
