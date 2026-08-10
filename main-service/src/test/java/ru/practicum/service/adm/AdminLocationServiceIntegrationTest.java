package ru.practicum.service.adm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AdminLocationServiceIntegrationTest {

    @Autowired
    private AdminLocationService adminLocationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private NewLocationDto newLocationDto;
    private UpdateLocationRequest updateRequest;
    private Location existingLocation;

    @BeforeEach
    void setUp() {
        existingLocation = new Location();
        existingLocation.setLat(55.7558);
        existingLocation.setLon(37.6173);
        existingLocation.setName("Existing Location");
        existingLocation.setDescription("Existing Description");
        existingLocation.setAddress("Existing Address");
        existingLocation.setStatus(LocationStatus.OFFICIAL);
        existingLocation = locationRepository.save(existingLocation);

        newLocationDto = new NewLocationDto();
        newLocationDto.setLat(56.7558);
        newLocationDto.setLon(38.6173);
        newLocationDto.setName("New Location");
        newLocationDto.setDescription("New Description");
        newLocationDto.setAddress("New Address");

        updateRequest = new UpdateLocationRequest();
        updateRequest.setLat(54.7558);
        updateRequest.setLon(36.6173);
        updateRequest.setName("Updated Location");
        updateRequest.setDescription("Updated Description");
        updateRequest.setAddress("Updated Address");
    }

    @Test
    void createLocationShouldReturnLocationDtoWhenValidRequest() {
        LocationDto result = adminLocationService.createLocation(newLocationDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(newLocationDto.getName());
        assertThat(result.getLat()).isEqualTo(newLocationDto.getLat());
        assertThat(result.getLon()).isEqualTo(newLocationDto.getLon());
        assertThat(result.getStatus()).isEqualTo(LocationStatus.OFFICIAL);

        Location savedLocation = locationRepository.findById(result.getId()).orElseThrow();
        assertThat(savedLocation.getName()).isEqualTo(newLocationDto.getName());
    }

    @Test
    void createLocationShouldThrowConflictExceptionWhenNameAlreadyExists() {
        newLocationDto.setName(existingLocation.getName());

        assertThatThrownBy(() -> adminLocationService.createLocation(newLocationDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Location with name '" + existingLocation.getName() + "' already exists");
    }

    @Test
    void createLocationShouldThrowConflictExceptionWhenCoordinatesAlreadyExist() {
        newLocationDto.setLat(existingLocation.getLat());
        newLocationDto.setLon(existingLocation.getLon());

        assertThatThrownBy(() -> adminLocationService.createLocation(newLocationDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Location with these coordinates already exists");
    }

    @Test
    void createLocationWithNullDescriptionAndAddressShouldSucceed() {
        newLocationDto.setDescription(null);
        newLocationDto.setAddress(null);

        LocationDto result = adminLocationService.createLocation(newLocationDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(newLocationDto.getName());
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAddress()).isNull();
    }

    @Test
    void updateLocationShouldReturnUpdatedLocationWhenValidRequest() {
        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingLocation.getId());
        assertThat(result.getName()).isEqualTo(updateRequest.getName());
        assertThat(result.getLat()).isEqualTo(updateRequest.getLat());
        assertThat(result.getLon()).isEqualTo(updateRequest.getLon());
        assertThat(result.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(result.getAddress()).isEqualTo(updateRequest.getAddress());
    }

    @Test
    void updateLocationShouldThrowNotFoundExceptionWhenLocationNotFound() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> adminLocationService.updateLocation(nonExistentId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateLocationShouldThrowConflictExceptionWhenNameAlreadyExists() {
        Location otherLocation = new Location();
        otherLocation.setName("Other Location");
        otherLocation.setLat(52.33);
        otherLocation.setLon(32.33);
        otherLocation.setStatus(LocationStatus.OFFICIAL);
        locationRepository.save(otherLocation);

        updateRequest.setName(otherLocation.getName());

        assertThatThrownBy(() -> adminLocationService.updateLocation(existingLocation.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Location with name 'Other Location' already exists");
    }

    @Test
    void updateLocationShouldThrowConflictExceptionWhenCoordinatesAlreadyExist() {
        updateRequest.setLat(existingLocation.getLat());
        updateRequest.setLon(existingLocation.getLon());

        assertThatThrownBy(() -> adminLocationService.updateLocation(existingLocation.getId(), updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Location with these coordinates already exists");
    }

    @Test
    void updateLocationShouldThrowValidationExceptionWhenOnlyLatProvided() {
        updateRequest.setLon(null);

        assertThatThrownBy(() -> adminLocationService.updateLocation(existingLocation.getId(), updateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void updateLocationShouldThrowValidationExceptionWhenOnlyLonProvided() {
        updateRequest.setLat(null);

        assertThatThrownBy(() -> adminLocationService.updateLocation(existingLocation.getId(), updateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Latitude and longitude must be provided together");
    }

    @Test
    void updateLocationShouldSucceedWhenAllFieldsNull() {
        UpdateLocationRequest emptyUpdate = new UpdateLocationRequest();

        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), emptyUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingLocation.getId());
        assertThat(result.getName()).isEqualTo(existingLocation.getName());
        assertThat(result.getDescription()).isEqualTo(existingLocation.getDescription());
        assertThat(result.getAddress()).isEqualTo(existingLocation.getAddress());
    }

    @Test
    void updateLocationShouldSucceedWhenOnlyNameProvided() {
        UpdateLocationRequest update = new UpdateLocationRequest();
        update.setName("Name Updated");

        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), update);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(update.getName());
        assertThat(result.getDescription()).isEqualTo(existingLocation.getDescription());
        assertThat(result.getAddress()).isEqualTo(existingLocation.getAddress());
    }

    @Test
    void updateLocationShouldSucceedWhenOnlyDescriptionProvided() {
        UpdateLocationRequest update = new UpdateLocationRequest();
        update.setDescription("Description Updated");

        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), update);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(existingLocation.getName());
        assertThat(result.getDescription()).isEqualTo(update.getDescription());
        assertThat(result.getAddress()).isEqualTo(existingLocation.getAddress());
    }

    @Test
    void updateLocationShouldSucceedWhenOnlyAddressProvided() {
        UpdateLocationRequest update = new UpdateLocationRequest();
        update.setAddress("Address Updated");

        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), update);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(existingLocation.getName());
        assertThat(result.getDescription()).isEqualTo(existingLocation.getDescription());
        assertThat(result.getAddress()).isEqualTo(update.getAddress());
    }

    @Test
    void updateLocationShouldSucceedWhenOnlyCoordinatesProvided() {
        UpdateLocationRequest update = new UpdateLocationRequest();
        update.setLat(25.7558);
        update.setLon(27.6173);

        LocationDto result = adminLocationService.updateLocation(existingLocation.getId(), update);

        assertThat(result).isNotNull();
        assertThat(result.getLat()).isEqualTo(update.getLat());
        assertThat(result.getLon()).isEqualTo(update.getLon());
        assertThat(result.getName()).isEqualTo(existingLocation.getName());
    }

    @Test
    void deleteLocationShouldDeleteWhenLocationExists() {
        adminLocationService.deleteLocation(existingLocation.getId());

        assertThat(locationRepository.findById(existingLocation.getId())).isEmpty();
    }

    @Test
    void deleteLocationShouldThrowNotFoundExceptionWhenLocationNotFound() {
        Long nonExistentId = 999999L;

        assertThatThrownBy(() -> adminLocationService.deleteLocation(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Location with id=" + nonExistentId + " was not found");
    }

    @Test
    void deleteLocationShouldThrowConflictExceptionWhenLocationHasLinkedEvents() {
        Category category = new Category();
        category.setName("category");
        categoryRepository.save(category);

        User user = new User();
        user.setName("user");
        user.setEmail("user@mail.ru");
        userRepository.save(user);

        Event event = new Event();
        event.setLocation(existingLocation);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setAnnotation("Test Annotation");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(user);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(false);
        event.setState(EventState.PENDING);
        eventRepository.save(event);

        assertThatThrownBy(() -> adminLocationService.deleteLocation(existingLocation.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete location with linked events");
    }
}