package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.request.RequestState;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipationRequestDto {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime created;

    private Long event;
    private Long id;
    private Long requester;
    private RequestState status;

}
