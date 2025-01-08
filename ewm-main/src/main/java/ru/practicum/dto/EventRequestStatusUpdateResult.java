package ru.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
    private final List<ParticipationRequestDto> confirmedRequests;
    private final List<ParticipationRequestDto> rejectedRequests;
}
