package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndpointHitDto {

    @NotBlank(message = "Название приложение должно быть заполнено")
    private String app;

    @NotBlank(message = "URI должен быть заполнен")
    private String uri;

    @NotBlank(message = "IP-адрес пользователя должен быть заполнен")
    private String ip;

    @NotNull(message = "Должна быть указана дата запроса")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
