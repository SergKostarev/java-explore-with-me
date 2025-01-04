package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.model.RequestStatus;
import ru.practicum.validation.ValueOfEnum;

import java.util.List;

@Data
public class EventRequestStatusUpdate {
    @NotNull
    private final List<@NotNull(message = "Request identifiers list contains null value") Long> requestIds;

    @NotNull
    @ValueOfEnum(enumClass = RequestStatus.class)
    private final String status;
}
