package ru.practicum.dto;

import lombok.Data;

@Data
public class ParticipationRequestDto {
    private final Long id;
    private final Long event;
    private final Long requester;
    private final String status;
    private final String created;
}
