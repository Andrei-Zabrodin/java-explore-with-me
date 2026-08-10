package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.NewLocationDto;
import ru.practicum.model.location.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location convertToEntity(NewLocationDto dto);

    LocationDto convertToDto(Location entity);
}
