package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.RequestState;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("SELECT DISTINCT pr FROM ParticipationRequest pr " +
            "JOIN FETCH pr.event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH pr.requester r " +
            "WHERE r.id = ?1")
    List<ParticipationRequest> findAllByRequesterIdWithFetch(Long userId);

    @Query("SELECT DISTINCT pr FROM ParticipationRequest pr " +
            "JOIN FETCH pr.event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH pr.requester r " +
            "WHERE e.id = ?1")
    List<ParticipationRequest> findAllByEventIdWithFetch(Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestState status);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);
}
