package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.location.Location;
import ru.practicum.model.location.LocationStatus;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT l FROM Location l " +
            "WHERE l.status = 'OFFICIAL' " +
            "AND ABS(cast(l.lat as double) - :lat) < :error " +
            "AND ABS(cast(l.lon as double) - :lon) < :error")
    Optional<Location> findByCoordinates(@Param("lat") Double lat,
                                         @Param("lon") Double lon,
                                         @Param("error") Double error);

    Optional<Location> findLocationByIdAndStatus(Long id, LocationStatus status);

    @Query("SELECT l FROM Location l " +
            "WHERE (CAST(:text AS text) IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%')) " +
            "OR LOWER(l.description) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%')) " +
            "OR LOWER(l.address) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%'))) " +
            "AND (CAST(CAST(:lat AS text) AS double) IS NULL OR CAST(CAST(:lon AS text) AS double) IS NULL " +
            "OR distance(CAST(CAST(:lat AS text) AS double), CAST(CAST(:lon AS text) AS double), l.lat, l.lon) <= :radius) " +
            "AND l.status = :status")
    Page<Location> findLocationsByPublicFilters(@Param("text") String text,
                                                @Param("lat") Double lat,
                                                @Param("lon") Double lon,
                                                @Param("radius") Double radius,
                                                @Param("status") LocationStatus status,
                                                Pageable pageable);
}
