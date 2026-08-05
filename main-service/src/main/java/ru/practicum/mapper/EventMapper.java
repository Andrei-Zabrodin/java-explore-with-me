package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.event.Event;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Mapping(target = "lat", source = "location.lat")
    @Mapping(target = "lon", source = "location.lon")
    @Mapping(target = "category", ignore = true)
    public abstract Event convertToEntity(NewEventDto dto);

    public EventFullDto convertToFullDto(Event event, Long views) {
        if (event == null) {
            return null;
        }

        EventFullDto dto = prepareForFullDto(event);
        dto.setViews(views);

        return dto;
    }

    public EventShortDto convertToShortDto(Event event, Long views) {
        if (event == null) {
            return null;
        }

        EventShortDto dto = prepareForShortDto(event);
        dto.setViews(views);

        return dto;
    }

    public List<EventShortDto> convertToShortDtoList(List<Event> events, Map<String, Long> viewsMap) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        return events.stream()
                .map(event -> {
                    EventShortDto dto = prepareForShortDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<EventFullDto> convertToFullDtoList(List<Event> events, Map<String, Long> viewsMap) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        return events.stream()
                .map(event -> {
                    EventFullDto dto = prepareForFullDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Mapping(target = "location.lat", source = "lat")
    @Mapping(target = "location.lon", source = "lon")
    protected abstract EventFullDto prepareForFullDto(Event entity);

    protected abstract EventShortDto prepareForShortDto(Event entity);
}
