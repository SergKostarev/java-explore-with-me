package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.RequestCountByEvent;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ru.practicum.model.EventState.PUBLISHED;
import static ru.practicum.model.RequestStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final UserService userService;

    private final RequestRepository requestRepository;

    private final EventRepository eventRepository;

    @Transactional()
    public ParticipationRequestDto post(long userId, long eventId) {
        User user = userService.getById(userId);
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId), eventId));
        if (event.getInitiator().equals(user)) { // инициатор события не может добавить запрос на участие в своём событии
            throw new ConditionsNotMetException(
                    String.format("Initiator with id=%d cannot participate in event with id=%d.", userId, eventId),
                    String.valueOf(eventId));
        }
        if (event.getState() != PUBLISHED) { // нельзя участвовать в неопубликованном событии
            throw new ConditionsNotMetException(
                    String.format("Participation in unpublished event with id=%d is forbidden.", eventId),
                    String.valueOf(eventId));
        }
        Optional<Request> existRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (existRequest.isPresent()) { // нельзя добавить повторный запрос
            throw new DataIntegrityException(
                    String.format("Request with user id=%d and event id=%d already exists.", userId, eventId),
                    String.valueOf(eventId));
        }
        int participantLimit = event.getParticipantLimit();
        if (participantLimit != 0 &&
                participantLimit <= (int) getConfirmedRequestsCount(eventId)) { // у события достигнут лимит запросов на участие
            throw new ConditionsNotMetException(
                    String.format("Event with id=%d participation limit is exceeded.", eventId), String.valueOf(eventId));
        }
        Request savedRequest = requestRepository.save(createRequest(user, event));
        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    public Request getById(long id) {
        Optional<Request> request = requestRepository.findById(id);
        if (request.isEmpty()) {
            throw new NotFoundException(String.format("Request with id=%d was not found", id), id);
        }
        return request.get();
    }

    public List<Request> getByIds(List<Long> ids) {
        return requestRepository.findAllById(ids);
    }

    public List<ParticipationRequestDto> getByRequesterId(long userId) {
        userService.getById(userId);
        return requestRepository.findByRequesterId(userId, Sort.by("id"))
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Transactional()
    public ParticipationRequestDto cancel(long userId, long requestId) {
        User user = userService.getById(userId);
        Request request = getById(requestId);
        if (!request.getRequester().equals(user)) {
            throw new ConditionsNotMetException(
                    String.format("User with id=%d is not authorized to cancel request with id=%d",
                    userId, requestId), String.valueOf(requestId));
        }
        request.setStatus(CANCELED);
        requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    public long getConfirmedRequestsCount(long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
    }

    public List<RequestCountByEvent> getConfirmedRequestsCount(List<Long> eventIds) {
        return requestRepository.countByEventIdsAndStatus(eventIds, CONFIRMED);
    }

    public List<ParticipationRequestDto> getByEventId(long eventId) {
        return requestRepository.findByEventId(eventId, Sort.by("id"))
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    public void saveRequests(Collection<Request> requests) {
        requestRepository.saveAll(requests);
    }

    private Request createRequest(User requester, Event event) {
        Request request = new Request();
        request.setRequester(requester);
        request.setEvent(event);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(CONFIRMED);
        } else {
            request.setStatus(PENDING);
        }
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
