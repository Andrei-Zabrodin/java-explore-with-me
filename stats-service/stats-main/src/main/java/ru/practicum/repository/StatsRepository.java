package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp >= :start " +
            "AND eh.timestamp <= :end " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(DISTINCT eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp >= :start " +
            "AND eh.timestamp <= :end " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllUniqueIpHits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp >= :start " +
            "AND eh.timestamp <= :end " +
            "AND eh.uri IN :uris " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllHitsByUri(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query("SELECT eh.app AS app, eh.uri AS uri, COUNT(DISTINCT eh.ip) AS hits FROM EndpointHit AS eh " +
            "WHERE eh.timestamp >= :start " +
            "AND eh.timestamp <= :end " +
            "AND eh.uri IN :uris " +
            "GROUP BY eh.app, eh.uri " +
            "ORDER BY hits DESC")
    List<ViewStats> findAllUniqueIpHitsByUri(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);
}
