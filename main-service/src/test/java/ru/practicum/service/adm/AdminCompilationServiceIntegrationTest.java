package ru.practicum.service.adm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class AdminCompilationServiceIntegrationTest {

    @Autowired
    private AdminCompilationService adminCompilationService;

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StatsClient statsClient;

    private User user;
    private Category category;
    private Event event1;
    private Event event2;
    private Compilation existingCompilation;
    private NewCompilationDto dto;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user = userRepository.save(user);

        category = new Category();
        category.setName("Category 1");
        category = categoryRepository.save(category);

        event1 = createEvent("Event 1", LocalDateTime.now().plusDays(5));
        event2 = createEvent("Event 2", LocalDateTime.now().plusDays(7));

        existingCompilation = new Compilation();
        existingCompilation.setTitle("Existing Compilation");
        existingCompilation.setPinned(true);
        existingCompilation.setEvents(new ArrayList<>(List.of(event1)));
        existingCompilation = compilationRepository.save(existingCompilation);

        dto = new NewCompilationDto();
        dto.setTitle("New Compilation");
        dto.setPinned(false);
        dto.setEvents(List.of(event1.getId(), event2.getId()));
    }

    @Test
    void createCompilationShouldReturnCompilationDtoWhenValid() {
        CompilationDto result = adminCompilationService.createCompilation(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo(dto.getTitle());
        assertThat(result.getPinned()).isEqualTo(dto.getPinned());
        assertThat(result.getEvents()).hasSize(dto.getEvents().size());

        Compilation savedComp = compilationRepository.findById(result.getId()).orElseThrow();
        assertThat(savedComp.getTitle()).isEqualTo(dto.getTitle());
        assertThat(savedComp.getEvents()).hasSize(dto.getEvents().size());
    }

    @Test
    void createCompilationShouldCreateWithoutEventsWhenEventsIsEmpty() {
        dto.setEvents(List.of());

        CompilationDto result = adminCompilationService.createCompilation(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEvents()).isEmpty();

        Compilation savedComp = compilationRepository.findById(result.getId()).orElseThrow();
        assertThat(savedComp.getEvents()).isEmpty();
    }

    @Test
    void createCompilationShouldCreateWithoutEventsWhenEventsIsNull() {
        dto.setEvents(null);

        CompilationDto result = adminCompilationService.createCompilation(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEvents()).isEmpty();

        Compilation savedComp = compilationRepository.findById(result.getId()).orElseThrow();
        assertThat(savedComp.getEvents()).isEmpty();
    }

    @Test
    void createCompilationShouldThrowNotFoundExceptionWhenEventNotFound() {
        dto.setEvents(List.of(999L));

        assertThatThrownBy(() -> adminCompilationService.createCompilation(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more events were not found");
    }

    @Test
    void updateCompilationShouldUpdateAllFieldsWhenValid() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Updated Compilation");
        request.setPinned(false);
        request.setEvents(List.of(event2.getId()));

        CompilationDto result = adminCompilationService.updateCompilation(request, existingCompilation.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingCompilation.getId());
        assertThat(result.getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getPinned()).isEqualTo(request.getPinned());
        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getEvents().get(0).getId()).isEqualTo(event2.getId());

        Compilation updatedComp = compilationRepository.findById(existingCompilation.getId()).orElseThrow();
        assertThat(updatedComp.getTitle()).isEqualTo(request.getTitle());
        assertThat(updatedComp.getPinned()).isEqualTo(request.getPinned());
        assertThat(updatedComp.getEvents()).hasSize(1);
    }

    @Test
    void updateCompilationShouldUpdateWhenPartialUpdate() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Updated Compilation");

        CompilationDto result = adminCompilationService.updateCompilation(request, existingCompilation.getId());

        assertThat(result.getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getPinned()).isEqualTo(existingCompilation.getPinned());
        assertThat(result.getEvents()).hasSize(1);

        Compilation updatedComp = compilationRepository.findById(existingCompilation.getId()).orElseThrow();
        assertThat(updatedComp.getTitle()).isEqualTo(request.getTitle());
        assertThat(updatedComp.getPinned()).isEqualTo(existingCompilation.getPinned());
        assertThat(updatedComp.getEvents()).hasSize(1);
    }

    @Test
    void updateCompilationShouldThrowNotFoundExceptionWhenCompilationNotFound() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Updated Compilation");

        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminCompilationService.updateCompilation(request, nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Compilation with id=" + nonExistentId + " was not found");
    }

    @Test
    void updateCompilationShouldThrowNotFoundExceptionWhenEventNotFound() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setEvents(List.of(999L));

        assertThatThrownBy(() -> adminCompilationService.updateCompilation(request, existingCompilation.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("One or more events were not found");
    }

    @Test
    void updateCompilationShouldRemoveAllEventsWhenEmptyList() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setEvents(List.of());

        CompilationDto result = adminCompilationService.updateCompilation(request, existingCompilation.getId());

        assertThat(result.getEvents()).isEmpty();

        Compilation updatedComp = compilationRepository.findById(existingCompilation.getId()).orElseThrow();
        assertThat(updatedComp.getEvents()).isEmpty();
    }

    @Test
    void deleteCompilationShouldDeleteWhenValidId() {
        Long compilationId = existingCompilation.getId();

        adminCompilationService.deleteCompilation(compilationId);

        assertThat(compilationRepository.findById(compilationId)).isEmpty();
    }

    @Test
    void deleteCompilationShouldThrowNotFoundExceptionWhenNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminCompilationService.deleteCompilation(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Compilation with id=" + nonExistentId + " was not found");
    }

    private Event createEvent(String title, LocalDateTime eventDate) {
        Event event = new Event();
        event.setTitle(title);
        event.setAnnotation("Annotation " + title);
        event.setDescription("Description " + title);
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        event.setEventDate(eventDate);
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event.setState(EventState.PUBLISHED);
        event.setLat(55.75);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        return eventRepository.save(event);
    }
}