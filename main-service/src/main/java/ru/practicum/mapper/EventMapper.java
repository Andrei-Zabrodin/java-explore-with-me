package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.event.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "location.lat", source = "lat")
    @Mapping(target = "location.lon", source = "lon")
    EventFullDto convertToFullDto(Event entity);

    EventShortDto convertToShortDto(Event entity);

    @Mapping(target = "lat", source = "location.lat")
    @Mapping(target = "lon", source = "location.lon")
    @Mapping(target = "category", ignore = true)
    Event convertToEntity(NewEventDto dto);

}
