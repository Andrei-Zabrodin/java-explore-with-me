package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllHits(LocalDateTime start, LocalDateTime end);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(DISTINCT eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllUniqueIpHits(LocalDateTime start, LocalDateTime end);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
            "AND eh.uri IN ?3 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllHitsByUri(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(DISTINCT eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp BETWEEN ?1 AND ?2 " +
            "AND eh.uri IN ?3 " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllUniqueIpHitsByUri(LocalDateTime start, LocalDateTime end, List<String> uris);
}
