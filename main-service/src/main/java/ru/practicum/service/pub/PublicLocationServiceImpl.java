package ru.practicum.service.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicLocationServiceImpl implements PublicLocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public List<LocationDto> getOfficialLocations(String text, Double lat, Double lon, Double radius, int from, int size) {
        if (lat != null && lon != null) {
            if (radius > 1000) {
                throw new ValidationException("Radius cannot exceed 1000 km");
            }
        } else if (lat != null || lon != null) {
            throw new ValidationException("Latitude and longitude must be provided together");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Location> locations = locationRepository.findLocationsByPublicFilters(text, lat, lon, radius, pageable)
                .getContent();

        log.debug("Найдено локаций: {}", locations.size());
        return locations.stream()
                .map(locationMapper::convertToDto)
                .toList();
    }

    @Override
    public LocationDto getOfficialLocationById(Long id) {
        Location location = locationRepository.findLocationByIdAndStatus(id, LocationStatus.OFFICIAL)
                .orElseThrow(() -> new NotFoundException("Official location with id=" + id + " was not found"));

        return locationMapper.convertToDto(location);
    }
}
