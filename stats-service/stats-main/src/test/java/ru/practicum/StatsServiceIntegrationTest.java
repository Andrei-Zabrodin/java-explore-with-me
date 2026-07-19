package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StatsServiceIntegrationTest {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private StatsService statsService;

    private LocalDateTime now;
    private EndpointHitDto hitDto1;
    private EndpointHitDto hitDto2;
    private EndpointHitDto hitDto3;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        hitDto1 = new EndpointHitDto();
        hitDto1.setApp("main-service");
        hitDto1.setUri("/events");
        hitDto1.setIp("127.0.0.1");
        hitDto1.setTimestamp(now.minusMinutes(5));

        hitDto2 = new EndpointHitDto();
        hitDto2.setApp("main-service");
        hitDto2.setUri("/events");
        hitDto2.setIp("127.0.0.1");
        hitDto2.setTimestamp(now.minusMinutes(3));

        hitDto3 = new EndpointHitDto();
        hitDto3.setApp("main-service");
        hitDto3.setUri("/events/5");
        hitDto3.setIp("127.0.0.1");
        hitDto3.setTimestamp(now.minusMinutes(1));
    }

    @Test
    void saveHitShouldSaveHit() {
        statsService.saveHit(hitDto1);

        List<EndpointHit> hits = statsRepository.findAll();
        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().getApp()).isEqualTo(hitDto1.getApp());
        assertThat(hits.getFirst().getUri()).isEqualTo(hitDto1.getUri());
        assertThat(hits.getFirst().getIp()).isEqualTo(hitDto1.getIp());
    }

    @Test
    void saveHitShouldSaveMultipleHits() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);
        statsService.saveHit(hitDto3);

        List<EndpointHit> hits = statsRepository.findAll();
        assertThat(hits).hasSize(3);
        assertThat(hits.get(0).getUri()).isEqualTo(hitDto1.getUri());
        assertThat(hits.get(0).getIp()).isEqualTo(hitDto1.getIp());
        assertThat(hits.get(1).getUri()).isEqualTo(hitDto2.getUri());
        assertThat(hits.get(1).getIp()).isEqualTo(hitDto2.getIp());
        assertThat(hits.get(2).getUri()).isEqualTo(hitDto3.getUri());
        assertThat(hits.get(2).getIp()).isEqualTo(hitDto3.getIp());
    }

    @Test
    void getStatsShouldReturnAllHitsWhenNoUrisAndUniqueFalse() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);
        statsService.saveHit(hitDto3);

        LocalDateTime start = now.minusMinutes(10);
        LocalDateTime end = now;

        List<ViewStatsDto> stats = statsService.getStats(start, end, null, false);

        assertThat(stats).hasSize(2);

        ViewStatsDto stat1 = stats.get(0);
        assertThat(stat1.getUri()).isEqualTo("/events");
        assertThat(stat1.getHits()).isEqualTo(2);

        ViewStatsDto stat2 = stats.get(1);
        assertThat(stat2.getUri()).isEqualTo("/events/5");
        assertThat(stat2.getHits()).isEqualTo(1);
    }

    @Test
    void getStatsShouldReturnUniqueHitsWhenNoUrisAndUniqueTrue() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);
        statsService.saveHit(hitDto3);

        LocalDateTime start = now.minusMinutes(10);
        LocalDateTime end = now;

        List<ViewStatsDto> stats = statsService.getStats(start, end, null, true);

        assertThat(stats).hasSize(2);

        ViewStatsDto stat1 = stats.get(0);
        assertThat(stat1.getUri()).isEqualTo("/events");
        assertThat(stat1.getHits()).isEqualTo(1);

        ViewStatsDto stat2 = stats.get(1);
        assertThat(stat2.getUri()).isEqualTo("/events/5");
        assertThat(stat2.getHits()).isEqualTo(1);
    }

    @Test
    void getStatsShouldReturnFilteredHitsWithUrisAndUniqueFalse() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);
        statsService.saveHit(hitDto3);

        LocalDateTime start = now.minusMinutes(10);
        LocalDateTime end = now;

        List<ViewStatsDto> stats = statsService.getStats(start, end, List.of("/events"), false);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getUri()).isEqualTo("/events");
        assertThat(stats.get(0).getHits()).isEqualTo(2);
    }

    @Test
    void getStatsShouldReturnFilteredUniqueHitsWithUrisAndUniqueTrue() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);
        statsService.saveHit(hitDto3);

        LocalDateTime start = now.minusMinutes(10);
        LocalDateTime end = now;

        List<ViewStatsDto> stats = statsService.getStats(start, end, List.of("/events"), true);

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getUri()).isEqualTo("/events");
        assertThat(stats.get(0).getHits()).isEqualTo(1);
    }

    @Test
    void getStatsShouldReturnEmptyListWhenNoHitsInTimeRange() {
        statsService.saveHit(hitDto1);
        statsService.saveHit(hitDto2);

        LocalDateTime start = now.minusMinutes(20);
        LocalDateTime end = now.minusMinutes(10);

        List<ViewStatsDto> stats = statsService.getStats(start, end, null, false);

        assertThat(stats).isEmpty();
    }

    @Test
    void getStatsShouldThrowValidationExceptionWhenStartAfterEnd() {
        LocalDateTime start = now.plusMinutes(10);
        LocalDateTime end = now.minusMinutes(10);

        assertThatThrownBy(() -> statsService.getStats(start, end, null, false))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата начала диапазона выгрузки должна быть не позже даты конца");
    }
}