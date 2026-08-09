package ru.practicum.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.model.location.LocationStatus;
import ru.practicum.service.pub.PublicLocationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicLocationController.class)
class PublicLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicLocationService publicLocationService;

    private LocationDto locationDto;

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
    }

    // ============ GET /location ============

    @Test
    void getOfficialLocationsShouldReturnListOfLocationsWithDefaultAndNullParams() throws Exception {
        List<LocationDto> locations = List.of(locationDto);
        when(publicLocationService.getOfficialLocations(null, null, null, 10.0, 0, 10))
                .thenReturn(locations);

        mockMvc.perform(get("/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(locationDto.getId()))
                .andExpect(jsonPath("$[0].name").value(locationDto.getName()))
                .andExpect(jsonPath("$[0].lat").value(locationDto.getLat()))
                .andExpect(jsonPath("$[0].lon").value(locationDto.getLon()))
                .andExpect(jsonPath("$[0].status").value(locationDto.getStatus().toString()));

        verify(publicLocationService, times(1))
                .getOfficialLocations(null, null, null, 10.0, 0, 10);
    }

    @Test
    void getOfficialLocationsWithAllParamsShouldReturnListOfLocations() throws Exception {
        List<LocationDto> locations = List.of(locationDto);
        when(publicLocationService.getOfficialLocations("test", 55.7558, 37.6173, 20.0, 5, 15))
                .thenReturn(locations);

        mockMvc.perform(get("/locations")
                        .param("text", "test")
                        .param("lat", "55.7558")
                        .param("lon", "37.6173")
                        .param("radius", "20.0")
                        .param("from", "5")
                        .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(publicLocationService, times(1))
                .getOfficialLocations("test", 55.7558, 37.6173, 20.0, 5, 15);
    }

    @Test
    void getOfficialLocationsWithInvalidRadiusShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("radius", "-1.0"))
                .andExpect(status().isBadRequest());

        verify(publicLocationService, never())
                .getOfficialLocations(anyString(), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    @Test
    void getOfficialLocationsWithInvalidFromShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(publicLocationService, never())
                .getOfficialLocations(anyString(), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    @Test
    void getOfficialLocationsWithInvalidSizeShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(publicLocationService, never())
                .getOfficialLocations(anyString(), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    // ============ GET /location/{id} ============

    @Test
    void getOfficialLocationByIdShouldReturnLocation() throws Exception {
        when(publicLocationService.getOfficialLocationById(1L)).thenReturn(locationDto);

        mockMvc.perform(get("/locations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationDto.getId()))
                .andExpect(jsonPath("$.name").value(locationDto.getName()))
                .andExpect(jsonPath("$.lat").value(locationDto.getLat()))
                .andExpect(jsonPath("$.lon").value(locationDto.getLon()))
                .andExpect(jsonPath("$.status").value(locationDto.getStatus().toString()));

        verify(publicLocationService, times(1)).getOfficialLocationById(1L);
    }

    @Test
    void getOfficialLocationByIdShouldReturnBadRequestWhenIdInvalid() throws Exception {
        mockMvc.perform(get("/locations/{id}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicLocationService, never()).getOfficialLocationById(anyLong());
    }
}