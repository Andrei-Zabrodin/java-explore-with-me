package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    @Query("SELECT c.id, e.id FROM Compilation c " +
            "JOIN c.events e " +
            "WHERE c.id IN ?1")
    List<Object[]> findCompilationEventIds(List<Long> compilationIds);
}
