package ru.practicum.controller.adm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.dto.location.UpdateLocationRequest;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.service.adm.AdminLocationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminLocationController.class)
class AdminLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminLocationService adminLocationService;

    private LocationDto locationDto;
    private NewLocationDto newLocationDto;
    private UpdateLocationRequest updateRequest;

    @BeforeEach
    void setUp() {
        locationDto = new LocationDto();
        locationDto.setId(1L);
        locationDto.setLat(55.7558);
        locationDto.setLon(37.6173);
        locationDto.setName("Test Location");
        locationDto.setDescription("Test Description");
        locationDto.setAddress("Test Address");
        locationDto.setStatus(LocationStatus.OFFICIAL);

        newLocationDto = new NewLocationDto();
        newLocationDto.setLat(55.7558);
        newLocationDto.setLon(37.6173);
        newLocationDto.setName("Test Location");
        newLocationDto.setDescription("Test Description");
        newLocationDto.setAddress("Test Address");

        updateRequest = new UpdateLocationRequest();
        updateRequest.setLat(55.7558);
        updateRequest.setLon(37.6173);
        updateRequest.setName("Updated Location");
        updateRequest.setDescription("Updated Description");
        updateRequest.setAddress("Updated Address");
    }

    // ============ GET /admin/locations ============

    @Test
    void getLocationsShouldReturnListOfLocations() throws Exception {
        List<LocationDto> locations = List.of(locationDto);
        when(adminLocationService.getLocations(0, 10)).thenReturn(locations);

        mockMvc.perform(get("/admin/locations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(locationDto.getId()))
                .andExpect(jsonPath("$[0].name").value(locationDto.getName()))
                .andExpect(jsonPath("$[0].lat").value(locationDto.getLat()))
                .andExpect(jsonPath("$[0].lon").value(locationDto.getLon()));

        verify(adminLocationService).getLocations(0, 10);
    }

    @Test
    void getLocations_withDefaultParams_shouldReturnListOfLocations() throws Exception {
        List<LocationDto> locations = List.of(locationDto);
        when(adminLocationService.getLocations(0, 10)).thenReturn(locations);

        mockMvc.perform(get("/admin/locations"))
                .andExpect(status().isOk());

        verify(adminLocationService, times(1)).getLocations(0, 10);
    }

    @Test
    void getLocationsShouldReturnBadRequestWhenFromIsInvalid() throws Exception {
        mockMvc.perform(get("/admin/locations")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).getLocations(anyInt(), anyInt());
    }

    @Test
    void getLocationsShouldReturnBadRequestWhenSizeIsInvalid() throws Exception {
        mockMvc.perform(get("/admin/locations")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).getLocations(anyInt(), anyInt());
    }

    // ============ GET /admin/locations/{locId} ============

    @Test
    void getLocationByIdShouldReturnLocation() throws Exception {
        when(adminLocationService.getLocationById(1L)).thenReturn(locationDto);

        mockMvc.perform(get("/admin/locations/{locId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationDto.getId()))
                .andExpect(jsonPath("$.name").value(locationDto.getName()))
                .andExpect(jsonPath("$.lat").value(locationDto.getLat()))
                .andExpect(jsonPath("$.lon").value(locationDto.getLon()));

        verify(adminLocationService, times(1)).getLocationById(1L);
    }

    @Test
    void getLocationByIdShouldReturnBadRequestWhenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/admin/locations/{locId}", "invalid id"))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).getLocationById(anyLong());
    }

    // ============ POST /admin/locations ============

    @Test
    void createLocationShouldReturnCreated() throws Exception {
        when(adminLocationService.createLocation(any(NewLocationDto.class))).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(locationDto.getId()))
                .andExpect(jsonPath("$.name").value(locationDto.getName()))
                .andExpect(jsonPath("$.lat").value(locationDto.getLat()))
                .andExpect(jsonPath("$.lon").value(locationDto.getLon()));

        verify(adminLocationService, times(1)).createLocation(any(NewLocationDto.class));
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLatIsNull() throws Exception {
        newLocationDto.setLat(null);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLatIsBelowMin() throws Exception {
        newLocationDto.setLat(-90.1);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLatIsAboveMax() throws Exception {
        newLocationDto.setLat(90.1);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLonIsNull() throws Exception {
        newLocationDto.setLon(null);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLonIsBelowMin() throws Exception {
        newLocationDto.setLon(-180.1);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenLonIsAboveMax() throws Exception {
        newLocationDto.setLon(180.1);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnCreatedWhenLonIsEqualMin() throws Exception {
        newLocationDto.setLon(-180.0);
        when(adminLocationService.createLocation(any(NewLocationDto.class))).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isCreated());

        verify(adminLocationService, times(1)).createLocation(any(NewLocationDto.class));
    }

    @Test
    void createLocationShouldReturnCreatedWhenLonIsEqualMax() throws Exception {
        newLocationDto.setLon(180.0);
        when(adminLocationService.createLocation(any(NewLocationDto.class))).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isCreated());

        verify(adminLocationService, times(1)).createLocation(any(NewLocationDto.class));
    }

    @Test
    void createLocationShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        newLocationDto.setName("");

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenNameIsNull() throws Exception {
        newLocationDto.setName(null);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenNameLongerThan120() throws Exception {
        newLocationDto.setName("a".repeat(121));

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnCreatedWhenNameIsAtMax() throws Exception {
        newLocationDto.setName("a".repeat(120));
        when(adminLocationService.createLocation(any(NewLocationDto.class))).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isCreated());

        verify(adminLocationService, times(1)).createLocation(any(NewLocationDto.class));
    }

    @Test
    void createLocationShouldReturnBadRequestWhenDescriptionIsLongerThan500() throws Exception {
        newLocationDto.setDescription("a".repeat(501));

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnBadRequestWhenAddressIsLongerThan500() throws Exception {
        newLocationDto.setAddress("a".repeat(501));

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).createLocation(any());
    }

    @Test
    void createLocationShouldReturnCreatedWhenDescriptionAndAddressAreNull() throws Exception {
        newLocationDto.setDescription(null);
        newLocationDto.setAddress(null);
        when(adminLocationService.createLocation(any(NewLocationDto.class))).thenReturn(locationDto);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLocationDto)))
                .andExpect(status().isCreated());

        verify(adminLocationService,times(1)).createLocation(any(NewLocationDto.class));
    }

    // ============ PATCH /admin/locations/{id} ============

    @Test
    void updateLocationShouldUpdateLocationWhenValidRequest() throws Exception {
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationDto.getId()))
                .andExpect(jsonPath("$.name").value(locationDto.getName()));

        verify(adminLocationService, times(1)).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenLatIsBelowMin() throws Exception {
        updateRequest.setLat(-90.1);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenLatIsAboveMax() throws Exception {
        updateRequest.setLat(90.1);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnOkWhenLatIsAtMin() throws Exception {
        updateRequest.setLat(-90.0);
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(adminLocationService, times(1)).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    @Test
    void updateLocationShouldReturnOkWhenLatIsAtMax() throws Exception {
        updateRequest.setLat(90.0);
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(adminLocationService, times(1)).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenLonIsBelowMin() throws Exception {
        updateRequest.setLon(-180.1);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenLonIsAboveMax() throws Exception {
        updateRequest.setLon(180.1);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnOkWhenLonIsAtMin() throws Exception {
        updateRequest.setLon(-180.0);
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(adminLocationService, times(1)).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    @Test
    void updateLocationShouldReturnOkWhenLonIsAtMax() throws Exception {
        updateRequest.setLon(180.0);
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(adminLocationService, times(1)).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenNameIsLongerThan120() throws Exception {
        updateRequest.setName("a".repeat(121));

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenDescriptionIsLongerThan500() throws Exception {
        updateRequest.setDescription("a".repeat(501));

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationShouldReturnBadRequestWhenAddressIsLongerThan500() throws Exception {
        updateRequest.setAddress("a".repeat(501));

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(adminLocationService, never()).updateLocation(any(), any());
    }

    @Test
    void updateLocationWithAllFieldsNullShouldReturnOk() throws Exception {
        UpdateLocationRequest emptyRequest = new UpdateLocationRequest();
        when(adminLocationService.updateLocation(eq(1L), any(UpdateLocationRequest.class))).thenReturn(locationDto);

        mockMvc.perform(patch("/admin/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk());

        verify(adminLocationService).updateLocation(eq(1L), any(UpdateLocationRequest.class));
    }

    // ============ DELETE /admin/locations/{id} ============

    @Test
    void deleteLocationShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/locations/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(adminLocationService, times(1)).deleteLocation(1L);
    }
}