package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CompilationMapper {

    @Mapping(target = "events", ignore = true)
    public abstract Compilation convertToEntity(NewCompilationDto dto);

    public CompilationDto convertToDto(Compilation compilation, List<EventShortDto> eventDtos) {
        if (compilation == null) {
            return null;
        }

        CompilationDto dto = prepareForDto(compilation);
        dto.setEvents(eventDtos);

        return dto;
    }

    public List<CompilationDto> convertToDtoList(List<Compilation> compilations, List<EventShortDto> eventDtoList,
                                                 Map<Long, List<Long>> compilationEventsMap) {
        if (compilations == null || compilations.isEmpty()) {
            return List.of();
        }

        if (eventDtoList == null || eventDtoList.isEmpty()) {
            return compilations.stream()
                    .map(this::prepareForDto)
                    .toList();
        }

        Map<Long, EventShortDto> eventDtoMap = eventDtoList.stream()
                .collect(Collectors.toMap(EventShortDto::getId, dto -> dto));

        return compilations.stream()
                .map(compilation -> {
                    CompilationDto dto = prepareForDto(compilation);
                    List<Long> compilationEventIds = compilationEventsMap.getOrDefault(compilation.getId(), List.of());
                    List<EventShortDto> compilationEvents = compilationEventIds.stream()
                            .map(eventDtoMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    dto.setEvents(compilationEvents);
                    return dto;
                })
                .toList();
    }

    @Mapping(target = "events", ignore = true)
    protected abstract CompilationDto prepareForDto(Compilation entity);
}
