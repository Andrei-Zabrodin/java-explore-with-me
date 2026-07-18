package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.ViewStatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = EndpointHitMapper.convertToEntity(hitDto);
        statsRepository.save(hit);

        log.debug("Сохранили в базу просмотр {}",hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала диапазона выгрузки должна быть не позже даты конца");
        }

        List<ViewStats> stats;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                stats = statsRepository.findAllUniqueIpHits(start, end);
            } else {
                stats = statsRepository.findAllHits(start, end);
            }
        } else {
            if (unique) {
                stats = statsRepository.findAllUniqueIpHitsByUri(start, end, uris);
            } else {
                stats = statsRepository.findAllHitsByUri(start, end, uris);
            }
        }

        return ViewStatsMapper.convertToDtoList(stats);
    }
}
