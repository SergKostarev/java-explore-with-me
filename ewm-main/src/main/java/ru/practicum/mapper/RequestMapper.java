package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Request;
import utils.DateUtils;

import java.util.List;

@UtilityClass
public class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus().name(),
                DateUtils.convertToString(request.getCreated())
        );
    }

    public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(List<Request> confirmedRequests,
                                                                                  List<Request> rejectedRequests) {
        return new EventRequestStatusUpdateResult(
                confirmedRequests
                        .stream()
                        .map(RequestMapper::toParticipationRequestDto)
                        .toList(),
                rejectedRequests
                        .stream()
                        .map(RequestMapper::toParticipationRequestDto)
                        .toList()
        );
    }

}
