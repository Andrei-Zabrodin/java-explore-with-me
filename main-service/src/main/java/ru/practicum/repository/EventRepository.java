package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategoryId(Long catId);

    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.id = ?1")
    Optional<Event> findByIdWithFetch(Long eventId);

    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.id IN ?1")
    List<Event> findAllByIdWithFetch(List<Long> eventIds);

    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:start AS timestamp) IS NULL OR e.eventDate >= :start) " +
            "AND (CAST(:end AS timestamp) IS NULL OR e.eventDate <= :end)")
    Page<Event> findEventsByAdminFilters(@Param("users") List<Long> users,
                                         @Param("states") List<EventState> states,
                                         @Param("categories") List<Long> categories,
                                         @Param("start") LocalDateTime rangeStart,
                                         @Param("end") LocalDateTime rangeEnd,
                                         Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (CAST(:text AS text) IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:paid AS boolean) IS NULL OR e.paid = :paid) " +
            "AND (CAST(:start AS timestamp) IS NULL OR e.eventDate >= :start) " +
            "AND (CAST(:end AS timestamp) IS NULL OR e.eventDate <= :end) " +
            "AND (:available = false OR e.participantLimit < e.confirmedRequests)")
    Page<Event> findEventsByPublicFilters(@Param("text") String text,
                                          @Param("categories") List<Long> categories,
                                          @Param("paid") Boolean paid,
                                          @Param("start") LocalDateTime rangeStart,
                                          @Param("end") LocalDateTime rangeEnd,
                                          @Param("available") Boolean onlyAvailable,
                                          Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator i " +
            "WHERE i.id = ?1 ")
    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);
}
