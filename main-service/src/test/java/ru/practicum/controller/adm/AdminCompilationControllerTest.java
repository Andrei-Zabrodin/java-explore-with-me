package ru.practicum.controller.adm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.service.adm.AdminCompilationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCompilationController.class)
class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminCompilationService adminCompilationService;

    private NewCompilationDto newCompilationDto;
    private UpdateCompilationRequest updateCompilationRequest;
    private CompilationDto compilationDto;
    private Long compilationId;

    @BeforeEach
    void setUp() {
        compilationId = 1L;

        newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("Летние концерты");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(List.of(1L, 2L, 3L));

        updateCompilationRequest = new UpdateCompilationRequest();
        updateCompilationRequest.setTitle("Зимние концерты");
        updateCompilationRequest.setPinned(false);

        compilationDto = new CompilationDto();
        compilationDto.setId(compilationId);
        compilationDto.setTitle("Летние концерты");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of());
    }

    // ============ POST /admin/compilations ============

    @Test
    void createCompilationShouldReturnCreatedWhenValidRequest() throws Exception {
        when(adminCompilationService.createCompilation(any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()));

        verify(adminCompilationService, times(1))
                .createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void createCompilationShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        newCompilationDto.setTitle("");

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest());

        verify(adminCompilationService, never())
                .createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void createCompilationShouldReturnBadRequestWhenTitleIsNull() throws Exception {
        newCompilationDto.setTitle(null);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest());

        verify(adminCompilationService, never())
                .createCompilation(any(NewCompilationDto.class));
    }

    @Test
    void createCompilationShouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        newCompilationDto.setTitle("A".repeat(60));

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isBadRequest());

        verify(adminCompilationService, never())
                .createCompilation(any(NewCompilationDto.class));
    }

    // ============ PATCH /admin/compilations/{compId} ============

    @Test
    void patchCompilationShouldReturnOkWhenValidRequest() throws Exception {
        CompilationDto updatedDto = new CompilationDto();
        updatedDto.setId(compilationId);
        updatedDto.setTitle("Зимние концерты");
        updatedDto.setPinned(false);
        updatedDto.setEvents(List.of());

        when(adminCompilationService.updateCompilation(any(UpdateCompilationRequest.class), eq(compilationId)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompilationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDto.getId()))
                .andExpect(jsonPath("$.title").value(updatedDto.getTitle()))
                .andExpect(jsonPath("$.pinned").value(updatedDto.getPinned()));

        verify(adminCompilationService, times(1))
                .updateCompilation(any(UpdateCompilationRequest.class), eq(compilationId));
    }

    @Test
    void patchCompilationShouldReturnBadRequestWhenTitleIsTooLong() throws Exception {
        updateCompilationRequest.setTitle("A".repeat(60));

        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompilationRequest)))
                .andExpect(status().isBadRequest());

        verify(adminCompilationService, never())
                .updateCompilation(any(UpdateCompilationRequest.class), eq(compilationId));
    }

    @Test
    void patchCompilationShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        updateCompilationRequest.setTitle("");

        mockMvc.perform(patch("/admin/compilations/{compId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompilationRequest)))
                .andExpect(status().isBadRequest());

        verify(adminCompilationService, never())
                .updateCompilation(any(UpdateCompilationRequest.class), eq(compilationId));
    }

    // ============ DELETE /admin/compilations/{compId} ============

    @Test
    void deleteCompilationShouldReturnNoContentWhenCompilationExists() throws Exception {
        doNothing().when(adminCompilationService).deleteCompilation(compilationId);

        mockMvc.perform(delete("/admin/compilations/{compId}", compilationId))
                .andExpect(status().isNoContent());

        verify(adminCompilationService, times(1)).deleteCompilation(compilationId);
    }

    @Test
    void deleteCompilationShouldReturnNotFoundWhenCompilationDoesNotExist() throws Exception {
        Long nonExistentId = 999L;
        doThrow(NotFoundException.class).when(adminCompilationService).deleteCompilation(nonExistentId);

        mockMvc.perform(delete("/admin/compilations/{compId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(adminCompilationService, never()).deleteCompilation(compilationId);
    }
}