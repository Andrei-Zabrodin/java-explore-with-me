package ru.practicum.service.pub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.compilation.CompilationDto;
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
class PublicCompilationServiceIntegrationTest {

    @Autowired
    private PublicCompilationService publicCompilationService;

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

    private Compilation pinnedCompilation;
    private Compilation notPinnedCompilation;
    private Event event;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        User user = new User();
        user.setEmail("user@example.com");
        user.setName("Test User");
        user = userRepository.save(user);

        Category category = new Category();
        category.setName("Category 1");
        category = categoryRepository.save(category);

        event = new Event();
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setCreatedOn(LocalDateTime.now());
        event.setPublishedOn(LocalDateTime.now());
        event.setState(EventState.PUBLISHED);
        event.setLat(55.75);
        event.setLon(37.62);
        event.setPaid(false);
        event.setParticipantLimit(10);
        event.setRequestModeration(true);
        event = eventRepository.save(event);

        pinnedCompilation = new Compilation();
        pinnedCompilation.setTitle("Pinned Compilation");
        pinnedCompilation.setPinned(true);
        pinnedCompilation.setEvents(new ArrayList<>(List.of(event)));
        pinnedCompilation = compilationRepository.save(pinnedCompilation);

        notPinnedCompilation = new Compilation();
        notPinnedCompilation.setTitle("Not Pinned Compilation");
        notPinnedCompilation.setPinned(false);
        notPinnedCompilation.setEvents(new ArrayList<>(List.of(event)));
        notPinnedCompilation = compilationRepository.save(notPinnedCompilation);
    }

    @Test
    void getCompilationsShouldReturnAllWhenPinnedIsNull() {
        List<CompilationDto> result = publicCompilationService.getCompilations(null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CompilationDto::getTitle)
                .containsExactlyInAnyOrder(pinnedCompilation.getTitle(), notPinnedCompilation.getTitle());
    }

    @Test
    void getCompilationsShouldReturnOnlyPinnedWhenPinnedIsTrue() {
        List<CompilationDto> result = publicCompilationService.getCompilations(true, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(pinnedCompilation.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(pinnedCompilation.getTitle());
        assertThat(result.get(0).getPinned()).isTrue();
        assertThat(result.get(0).getEvents().get(0).getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void getCompilationsShouldReturnOnlyNotPinnedWhenPinnedIsFalse() {
        List<CompilationDto> result = publicCompilationService.getCompilations(false, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(notPinnedCompilation.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(notPinnedCompilation.getTitle());
        assertThat(result.get(0).getPinned()).isFalse();
        assertThat(result.get(0).getEvents().get(0).getTitle()).isEqualTo(event.getTitle());
    }

    @Test
    void getCompilationsShouldReturnEmptyListWhenNoCompilations() {
        notPinnedCompilation.setPinned(true);
        compilationRepository.save(notPinnedCompilation);

        List<CompilationDto> result = publicCompilationService.getCompilations(false, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getCompilationsShouldReturnCompilationsWithPagination() {
        List<CompilationDto> firstPage = publicCompilationService.getCompilations(null, 0, 1);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).getTitle()).isEqualTo(pinnedCompilation.getTitle());

        List<CompilationDto> secondPage = publicCompilationService.getCompilations(null, 1, 1);

        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getTitle()).isEqualTo(notPinnedCompilation.getTitle());

        List<CompilationDto> thirdPage = publicCompilationService.getCompilations(null, 2, 1);

        assertThat(thirdPage).isEmpty();
    }

    @Test
    void getCompilationByIdShouldReturnCompilation() {
        CompilationDto result = publicCompilationService.getCompilationById(pinnedCompilation.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(pinnedCompilation.getId());
        assertThat(result.getTitle()).isEqualTo(pinnedCompilation.getTitle());
        assertThat(result.getPinned()).isTrue();
        assertThat(result.getEvents()).hasSize(1);
    }

    @Test
    void getCompilationByIdShouldThrowNotFoundExceptionWhenNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> publicCompilationService.getCompilationById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Compilation with id=" + nonExistentId + " was not found");
    }
}