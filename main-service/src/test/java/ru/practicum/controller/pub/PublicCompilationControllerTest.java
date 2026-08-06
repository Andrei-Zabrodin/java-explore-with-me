package ru.practicum.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.pub.PublicCompilationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicCompilationController.class)
class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicCompilationService publicCompilationService;

    private Long compilationId;
    private CompilationDto compilationDto;

    @BeforeEach
    void setUp() {
        compilationId = 1L;

        compilationDto = new CompilationDto();
        compilationDto.setId(compilationId);
        compilationDto.setTitle("Летние концерты");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of());
    }

    // ============ GET /compilations ============

    @Test
    void getCompilationsShouldReturnListWhenValidRequest() throws Exception {
        List<CompilationDto> compilations = List.of(compilationDto);

        when(publicCompilationService.getCompilations(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(compilations);

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(compilationDto.getId()))
                .andExpect(jsonPath("$[0].title").value(compilationDto.getTitle()))
                .andExpect(jsonPath("$[0].pinned").value(compilationDto.getPinned()));

        verify(publicCompilationService, times(1))
                .getCompilations(eq(true), eq(0), eq(10));
    }

    @Test
    void getCompilationsShouldReturnListWhenPinnedIsNull() throws Exception {
        List<CompilationDto> compilations = List.of(compilationDto);

        when(publicCompilationService.getCompilations(isNull(), anyInt(), anyInt()))
                .thenReturn(compilations);

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(publicCompilationService, times(1))
                .getCompilations(isNull(), anyInt(), anyInt());
    }

    @Test
    void getCompilationsShouldReturnOkWithDefaultParameters() throws Exception {
        List<CompilationDto> compilations = List.of(compilationDto);

        when(publicCompilationService.getCompilations(isNull(), eq(0), eq(10)))
                .thenReturn(compilations);

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk());

        verify(publicCompilationService, times(1))
                .getCompilations(isNull(), eq(0), eq(10));
    }

    @Test
    void getCompilationsShouldReturnBadRequestWhenInvalidFrom() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCompilationService, never())
                .getCompilations(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void getCompilationsShouldReturnBadRequestWhenInvalidSize() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("size", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCompilationService, never())
                .getCompilations(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void getCompilationsShouldReturnBadRequestWhenNegativeFrom() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(publicCompilationService, never())
                .getCompilations(anyBoolean(), anyInt(), anyInt());
    }

    @Test
    void getCompilationsShouldReturnBadRequestWhenZeroSize() throws Exception {
        mockMvc.perform(get("/compilations")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(publicCompilationService, never())
               .getCompilations(anyBoolean(), anyInt(), anyInt());
    }

    // ============ GET /compilations/{compId} ============

    @Test
    void getCompilationByIdShouldReturnCompilationWhenValidRequest() throws Exception {
        when(publicCompilationService.getCompilationById(eq(compilationId)))
                .thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/{compId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()));

        verify(publicCompilationService, times(1))
                .getCompilationById(eq(compilationId));
    }

    @Test
    void getCompilationByIdShouldReturnBadRequestWhenInvalidCompilationId() throws Exception {
        mockMvc.perform(get("/compilations/{compId}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(publicCompilationService, never())
                .getCompilationById(anyLong());
    }
}