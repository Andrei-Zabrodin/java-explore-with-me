package ru.practicum.service.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.repository.LocationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PublicLocationServiceIntegrationTest {

    @Autowired
    private PublicLocationService publicLocationService;

    @Autowired
    private LocationRepository locationRepository;

    private Location officialLocation1;
    private Location officialLocation2;
    private Location customLocation;

    @BeforeEach
    void setUp() {
        officialLocation1 = new Location();
        officialLocation1.setLat(55.7558);
        officialLocation1.setLon(37.6173);
        officialLocation1.setName("Location 1");
        officialLocation1.setDescription("Description 1");
        officialLocation1.setAddress("Address 1");
        officialLocation1.setStatus(LocationStatus.OFFICIAL);
        officialLocation1 = locationRepository.save(officialLocation1);

        officialLocation2 = new Location();
        officialLocation2.setLat(59.9343);
        officialLocation2.setLon(30.3351);
        officialLocation2.setName("Location 2");
        officialLocation2.setDescription("Description 2");
        officialLocation2.setAddress("Address 2");
        officialLocation2.setStatus(LocationStatus.OFFICIAL);
        officialLocation2 = locationRepository.save(officialLocation2);

        customLocation = new Location();
        customLocation.setLat(55.7558);
        customLocation.setLon(37.6173);
        customLocation.setName("Custom Location");
        customLocation.setDescription("Custom Description");
        customLocation.setAddress("Custom Address");
        customLocation.setStatus(LocationStatus.CUSTOM);
        customLocation = locationRepository.save(customLocation);
    }

    @Test
    void getOfficialLocationsShouldReturnAllOfficialLocations() {
        List<LocationDto> result = publicLocationService.getOfficialLocations(null, null, null, null, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).extracting(LocationDto::getName)
                .contains(officialLocation1.getName(), officialLocation2.getName());
        assertThat(result).extracting(LocationDto::getStatus)
                .allMatch(status -> status == LocationStatus.OFFICIAL);
        assertThat(result).extracting(LocationDto::getName)
                .doesNotContain(customLocation.getName());
    }

    @Test
    void getOfficialLocationsShouldFilterByText() {
        List<LocationDto> result = publicLocationService.getOfficialLocations("1", null, null, null, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(officialLocation1.getName());
        assertThat(result.get(0).getStatus()).isEqualTo(LocationStatus.OFFICIAL);
    }

    @Test
    void getOfficialLocationsShouldReturnEmptyListWhenTextNotFound() {
        List<LocationDto> result = publicLocationService.getOfficialLocations("NonExistentLocation", null, null, null, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getOfficialLocationsShouldFilterByCoordinatesAndRadius() {
        List<LocationDto> result = publicLocationService.getOfficialLocations(
                null, officialLocation1.getLat() + 0.1, officialLocation1.getLon() + 0.1, 100.0, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(LocationDto::getName)
                .contains(officialLocation1.getName());
    }

    @Test
    void getOfficialLocationsShouldReturnEmptyListWhenCoordinatesOutOfRadius() {
        List<LocationDto> result = publicLocationService.getOfficialLocations(
                null, officialLocation1.getLat() + 0.1, officialLocation1.getLon() + 0.1, 1.0, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getOfficialLocationsShouldThrowValidationExceptionWhenOnlyLatProvided() {
        assertThatThrownBy(() -> publicLocationService.getOfficialLocations(null, 55.7558, null, null, 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void getOfficialLocationsShouldThrowValidationExceptionWhenOnlyLonProvided() {
        assertThatThrownBy(() -> publicLocationService.getOfficialLocations(null, null, 37.6173, null, 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void getOfficialLocationsShouldThrowValidationExceptionWhenRadiusExceedsLimit() {
        assertThatThrownBy(() -> publicLocationService.getOfficialLocations(null, 55.7558, 37.6173, 1000.1, 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Radius cannot exceed 1000 km");
    }

    @Test
    void getOfficialLocationsShouldWorkWithRadiusExactlyAtLimit() {
        List<LocationDto> result = publicLocationService.getOfficialLocations(
                null, 55.7558, 37.6173, 1000.0, 0, 10);

        assertThat(result).isNotNull();
    }

    @Test
    void getOfficialLocationsShouldSupportPagination() {
        for (int i = 0; i < 5; i++) {
            Location location = new Location();
            location.setLat(55.7558 + i * 0.1);
            location.setLon(37.6173 + i * 0.1);
            location.setName("New Location " + i);
            location.setStatus(LocationStatus.OFFICIAL);
            locationRepository.save(location);
        }

        List<LocationDto> firstPage = publicLocationService.getOfficialLocations(null, null, null, null, 0, 3);
        List<LocationDto> secondPage = publicLocationService.getOfficialLocations(null, null, null, null, 3, 3);

        assertThat(firstPage).hasSize(3);
        assertThat(secondPage).hasSize(3);
        assertThat(firstPage).extracting(LocationDto::getName)
                .containsExactlyInAnyOrder(officialLocation1.getName(), officialLocation2.getName(), "New Location 0");
        assertThat(secondPage).extracting(LocationDto::getName)
                .containsExactlyInAnyOrder("New Location 1", "New Location 2", "New Location 3");
    }

    @Test
    void getOfficialLocationByIdShouldReturnLocationWhenExists() {
        LocationDto result = publicLocationService.getOfficialLocationById(officialLocation1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(officialLocation1.getId());
        assertThat(result.getName()).isEqualTo(officialLocation1.getName());
        assertThat(result.getStatus()).isEqualTo(LocationStatus.OFFICIAL);
    }

    @Test
    void getOfficialLocationByIdShouldThrowNotFoundExceptionWhenLocationNotFound() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> publicLocationService.getOfficialLocationById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Official location with id=" + nonExistentId + " was not found");
    }

    @Test
    void getOfficialLocationByIdShouldThrowNotFoundExceptionWhenLocationIsNotOfficial() {
        assertThatThrownBy(() -> publicLocationService.getOfficialLocationById(customLocation.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Official location with id=" + customLocation.getId() + " was not found");
    }

    @Test
    void getOfficialLocationsShouldCombineTextAndCoordinatesFilters() {
        Location location = new Location();
        location.setLat(65.7558);
        location.setLon(47.6173);
        location.setName("Test Location");
        location.setStatus(LocationStatus.OFFICIAL);
        locationRepository.save(location);

        List<LocationDto> result = publicLocationService.getOfficialLocations(
                "Test", 65.9, 47.6, 100.0, 0, 10);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(LocationDto::getName)
                .contains(location.getName());
    }
}