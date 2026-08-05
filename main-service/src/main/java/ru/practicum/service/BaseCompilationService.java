package ru.practicum.service;

import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCompilationService {

    protected final CompilationRepository compilationRepository;

    protected BaseCompilationService(CompilationRepository compilationRepository) {
        this.compilationRepository = compilationRepository;
    }

    protected Map<Long, List<Long>> getCompilationEventsMap(List<Compilation> compilations) {

        List<Long> compilationIds = compilations.stream()
                .map(Compilation::getId)
                .toList();

        List<Object[]> results = compilationRepository.findCompilationEventIds(compilationIds);

        Map<Long, List<Long>> map = new HashMap<>();
        for (Object[] elem : results) {
            Long compilationId = (Long) elem[0];
            Long eventId = (Long) elem[1];
            map.computeIfAbsent(compilationId, k -> new ArrayList<>()).add(eventId);
        }

        return map;
    }

    protected List<Long> getCompilationEventsList(Compilation compilation) {
        Long compilationId = compilation.getId();
        List<Object[]> results = compilationRepository.findCompilationEventIds(List.of(compilationId));

        List<Long> eventIds = new ArrayList<>();
        for (Object[] elem : results) {
            Long eventId = (Long) elem[1];
            eventIds.add(eventId);
        }

        return eventIds;
    }
}
