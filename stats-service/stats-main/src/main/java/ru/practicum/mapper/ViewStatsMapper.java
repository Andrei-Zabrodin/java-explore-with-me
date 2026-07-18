package ru.practicum.mapper;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.ViewStats;

import java.util.List;

public class ViewStatsMapper {
    public static ViewStatsDto convertToDto(ViewStats entity) {
        if (entity == null) {
            return null;
        }

        ViewStatsDto dto = new ViewStatsDto();

        dto.setApp(entity.getApp());
        dto.setUri(entity.getUri());
        dto.setHits(entity.getHits());

        return dto;
    }

    public static List<ViewStatsDto> convertToDtoList(List<ViewStats> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(ViewStatsMapper::convertToDto)
                .toList();
    }
}
