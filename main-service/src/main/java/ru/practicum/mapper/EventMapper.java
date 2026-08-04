package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.event.Event;
import ru.practicum.service.ViewsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Autowired
    private ViewsService viewsService;

    @Mapping(target = "lat", source = "location.lat")
    @Mapping(target = "lon", source = "location.lon")
    @Mapping(target = "category", ignore = true)
    public abstract Event convertToEntity(NewEventDto dto);

    public EventFullDto convertToFullDto(Event event) {
        return convertToFullDto(event, null, null);
    }

    public EventShortDto convertToShortDto(Event event) {
        return convertToShortDto(event, null, null);
    }

    public List<EventShortDto> convertToShortDtoList(List<Event> events) {
        return convertToShortDtoList(events, null, null);
    }

    public List<EventFullDto> convertToFullDtoList(List<Event> events) {
        return convertToFullDtoList(events, null, null);
    }

    public EventFullDto convertToFullDto(Event event, LocalDateTime start, LocalDateTime end) {
        EventFullDto dto = prepareForFullDto(event);

        Long views = viewsService.getViewsForEvent(event, start, end);
        dto.setViews(views);

        return dto;
    }

    public EventShortDto convertToShortDto(Event event, LocalDateTime start, LocalDateTime end) {
        EventShortDto dto = prepareForShortDto(event);

        Long views = viewsService.getViewsForEvent(event, start, end);
        dto.setViews(views);

        return dto;
    }

    public List<EventShortDto> convertToShortDtoList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = viewsService.getViewsMap(events, start, end);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = prepareForShortDto(event);
                    dto.setViews(viewsMap.getOrDefault("/events/" + event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<EventFullDto> convertToFullDtoList(List<Event> events, LocalDateTime start, LocalDateTime end) {
        Map<String, Long> viewsMap = viewsService.getViewsMap(events, start, end);

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
