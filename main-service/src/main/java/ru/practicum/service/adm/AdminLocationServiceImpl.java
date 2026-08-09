package ru.practicum.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLocationServiceImpl implements AdminLocationService {
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final LocationMapper locationMapper;

    private static final double COORDINATES_ERROR = 0.001;

    @Override
    public List<LocationDto> getLocations(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Location> locations = locationRepository.findAll(pageable).getContent();

        log.debug("Найдено локаций: {}", locations.size());
        return locations.stream()
                .map(locationMapper::convertToDto)
                .toList();
    }

    @Override
    public LocationDto getLocationById(Long locId) {
        Location location = locationRepository.findById(locId)
                .orElseThrow(() -> new NotFoundException("Location with id=" + locId + " was not found"));
        return locationMapper.convertToDto(location);
    }

    @Override
    public LocationDto createLocation(NewLocationDto dto) {
        // Проверяем уникальность названия
        if (locationRepository.existsByName(dto.getName())) {
            throw new ConflictException("Location with name '" + dto.getName() + "' already exists");
        }

        // Проверяем, существует ли уже локация с такими координатами
        Optional<Location> existingLocation = locationRepository.findByCoordinates(
                dto.getLat(), dto.getLon(), COORDINATES_ERROR
        );
        if (existingLocation.isPresent()) {
            throw new ConflictException("Location with these coordinates already exists");
        }

        Location location = locationMapper.convertToEntity(dto);
        location.setStatus(LocationStatus.OFFICIAL);
        Location savedLocation = locationRepository.save(location);
        log.info("Created location with id: {}", savedLocation.getId());

        return locationMapper.convertToDto(savedLocation);
    }

    @Override
    public LocationDto updateLocation(Long id, UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location with id=" + id + " was not found"));

        if (request.getName() != null) {
            // Проверяем уникальность названия (исключая текущую локацию)
            if (locationRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new ConflictException("Location with name '" + request.getName() + "' already exists");
            }
            location.setName(request.getName());
        }

        if (request.getLat() != null && request.getLon() != null) {
            // Проверяем, существует ли уже локация с такими координатами
            Optional<Location> existingLocation = locationRepository.findByCoordinates(
                    request.getLat(), request.getLon(), COORDINATES_ERROR
            );
            if (existingLocation.isPresent()) {
                throw new ConflictException("Location with these coordinates already exists");
            }

            location.setLat(request.getLat());
            location.setLon(request.getLon());
        } else if (request.getLat() != null || request.getLon() != null) {
            throw new ValidationException("Latitude and longitude must be provided together");
        }

        Optional.ofNullable(request.getDescription()).ifPresent(location::setDescription);
        Optional.ofNullable(request.getAddress()).ifPresent(location::setAddress);

        Location updatedLocation = locationRepository.save(location);
        log.info("Updated location with id: {}", updatedLocation.getId());

        return locationMapper.convertToDto(updatedLocation);
    }

    @Override
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location with id=" + id + " was not found"));

        // Проверяем, есть ли события, привязанные к этой локации
        if (eventRepository.existsByLocationId(id)) {
             throw new ConflictException("Cannot delete location with linked events");
        }

        locationRepository.delete(location);
        log.info("Deleted location with id: {}", id);
    }
}
