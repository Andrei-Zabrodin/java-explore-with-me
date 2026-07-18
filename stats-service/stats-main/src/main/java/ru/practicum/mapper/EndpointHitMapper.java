package ru.practicum.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

public class EndpointHitMapper {
    public static EndpointHit convertToEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }

        EndpointHit hit = new EndpointHit();

        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());

        return hit;
    }
}
