package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategoryId(Long catId);

    @Query("SELECT e FROM Event e " +
            "WHERE (?1 IS NULL OR e.initiator.id IN ?1) " +
            "AND (?2 IS NULL OR e.state IN ?2) " +
            "AND (?3 IS NULL OR e.category.id IN ?3) " +
            "AND (?4 IS NULL OR e.eventDate >= ?4) " +
            "AND (?5 IS NULL OR e.eventDate <= ?5)")
    Page<Event> findEventsByAdminFilters(List<Long> users, List<EventState> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (?1 IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) " +
            "AND (?2 IS NULL OR e.category.id IN ?2) " +
            "AND (?3 IS NULL OR e.paid = ?3) " +
            "AND (?4 IS NULL OR e.eventDate >= ?4) " +
            "AND (?5 IS NULL OR e.eventDate <= ?5) " +
            "AND (?6 = false OR e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)")
    Page<Event> findEventsByPublicFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
            LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable pageable);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);
}
