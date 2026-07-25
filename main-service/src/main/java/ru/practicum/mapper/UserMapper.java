package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

@Mapper
public interface UserMapper {
    UserShortDto convertToShortDto(User entity);

    User convertToEntity(UserShortDto dto);
}
