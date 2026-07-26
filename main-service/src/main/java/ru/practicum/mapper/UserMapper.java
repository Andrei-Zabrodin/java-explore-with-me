package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

@Mapper
public interface UserMapper {
    UserShortDto convertToShortDto(User entity);

    UserDto convertToDto(User entity);

    User convertToEntity(NewUserRequest dto);
}
