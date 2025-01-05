package ru.practicum.service;

import dto.ViewStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.EventSpecification;
import ru.practicum.dao.LocationRepository;
import ru.practicum.dto.*;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.IncorrectDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import utils.DateUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.dto.SortingType.EVENT_DATE;
import static ru.practicum.dto.SortingType.VIEWS;
import static ru.practicum.dto.StateAction.*;
import static ru.practicum.model.EventState.*;
import static ru.practicum.model.RequestStatus.CONFIRMED;
import static ru.practicum.model.RequestStatus.REJECTED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final UserService userService;

    private final CategoryService categoryService;

    private final RequestService requestService;

    private final EventRepository eventRepository;

    private final LocationRepository locationRepository;

    private final StatsClient statsClient;

    @Transactional()
    public EventFullDto post(long userId, NewEventDto newEventDto) {
        User user = userService.getById(userId);
        Category category = categoryService.getById(newEventDto.getCategory());
        validateLocationDto(newEventDto.getLocation());
        validateEventDate(newEventDto.getEventDate(), 2L);
        Location location = locationRepository.save(EventMapper.toLocation(newEventDto.getLocation()));
        Event savedEvent = eventRepository.save(EventMapper.fromNewEventDtoToEvent(newEventDto, category, user, location));
        return EventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    public Event getById(long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Event with id=%d was not found", id), id);
        }
        return event.get();
    }

    public List<EventShortDto> getEventsByUserId(long userId, int from, int size) {
        userService.getById(userId);
        List<Event> events = eventRepository.findByInitiatorId(userId, from, size);
        return getEventShortDto(events);
    }

    public List<EventShortDto> getEventShortDto(Collection<Event> events) {
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Long> requestCountMap = getConfirmedRequestCountMap(events);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e,
                        requestCountMap.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    public EventFullDto getFullEventByUserId(long userId, long eventId) {
        User user = userService.getById(userId);
        Event event = getById(eventId);
        userIsAuthorizedCheck(user, event);
        return EventMapper.toEventFullDto(event,
                requestService.getConfirmedRequestsCount(event.getId()), getViews(event));
    }

    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId) {
        Event event = getById(eventId);
        User user = userService.getById(userId);
        userIsAuthorizedCheck(user, event);
        return requestService.getByEventId(eventId);
    }

    @Transactional()
    public EventRequestStatusUpdateResult changeRequestStatus(long userId, long eventId,
                                                              EventRequestStatusUpdate requestStatusUpdate) {
        Event event = getById(eventId);
        User user = userService.getById(userId);
        userIsAuthorizedCheck(user, event);
        RequestStatus status = RequestStatus.valueOf(requestStatusUpdate.getStatus());
        if (status != CONFIRMED && status != REJECTED) {
            throw new IncorrectDataException(
                    String.format("Incorrect status %s value.", status),
                    status.toString());
        }
        List<Long> requestIds = requestStatusUpdate
                .getRequestIds()
                .stream()
                .distinct()
                .toList();
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) { // для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
            return RequestMapper.toEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        }
        int requestCount = (int) requestService.getConfirmedRequestsCount(eventId);
        if (event.getParticipantLimit() != 0 &&
                event.getParticipantLimit() <= requestCount) { // у события достигнут лимит запросов на участие
            throw new ConditionsNotMetException(
                    String.format("Event with id=%d participation limit is exceeded.", eventId), String.valueOf(eventId));
        }
        Map<Long, Request> requests = requestService.getByIds(requestIds)
                .stream()
                .collect(Collectors.toMap(Request::getId, Function.identity()));
        int confirmedCount = 0;
        for (Long requestId: requestIds) {
            Request request = requests.get(requestId);
            if (request == null) {
                throw new NotFoundException(String.format("Request with id=%d was not found", requestId), requestId);
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConditionsNotMetException(String.format("Request with id=%d must have status PENDING", requestId),
                        String.valueOf(requestId));
            }
            RequestStatus statusToSet = status;
            if (status == CONFIRMED && event.getParticipantLimit() <= requestCount + confirmedCount) {
                statusToSet = REJECTED;
            } else if (status == CONFIRMED) {
                confirmedCount++;
            }
            request.setStatus(statusToSet);
            if (statusToSet == CONFIRMED) {
                confirmedRequests.add(request);
            } else {
                rejectedRequests.add(request);
            }
        }
        requestService.saveRequests(requests.values());
        return RequestMapper.toEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Transactional()
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getById(eventId);
        User user = userService.getById(userId);
        userIsAuthorizedCheck(user, event);
        EventState currentState = event.getState();
        validateEventDate(DateUtils.convertToString(event.getEventDate()), 2L);
        if (currentState == PUBLISHED) {
            throw new ConditionsNotMetException(String.format("Event with id=%d cannot not be published", eventId),
                    String.valueOf(eventId));
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategoryId() != null) {
            Category category = categoryService.getById(updateEvent.getCategoryId());
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            validateEventDate(updateEvent.getEventDate(), 2L);
            event.setEventDate(DateUtils.convertToDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getLocation() != null) {
            validateLocationDto(updateEvent.getLocation());
            Location location = event.getLocation();
            location.setLat(updateEvent.getLocation().getLat());
            location.setLon(updateEvent.getLocation().getLon());
            locationRepository.save(location);
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(updateEvent.getStateAction());
            if (currentState == CANCELED && stateAction == SEND_TO_REVIEW) {
                event.setState(PENDING);
            } else if (currentState == PENDING && stateAction == CANCEL_REVIEW) {
                event.setState(CANCELED);
            }
        }
        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(updatedEvent,
                requestService.getConfirmedRequestsCount(updatedEvent.getId()), getViews(updatedEvent));
    }

    public EventFullDto getEvent(long id) {
        Event event = getById(id);
        if (event.getState() != PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d is not published", id), id);
        }
        return EventMapper.toEventFullDto(event,
                requestService.getConfirmedRequestsCount(event.getId()), getViews(event));
    }

    @Transactional()
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = getById(eventId);
        EventState currentState = event.getState();
        validateEventDate(DateUtils.convertToString(event.getEventDate()), 1L);
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategoryId() != null) {
            Category category = categoryService.getById(updateEvent.getCategoryId());
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            validateEventDate(updateEvent.getEventDate(), 1L);
            event.setEventDate(DateUtils.convertToDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getLocation() != null) {
            validateLocationDto(updateEvent.getLocation());
            Location location = event.getLocation();
            location.setLat(updateEvent.getLocation().getLat());
            location.setLon(updateEvent.getLocation().getLon());
            locationRepository.save(location);
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(updateEvent.getStateAction());
            if (stateAction == PUBLISH_EVENT) {
                if (currentState != PENDING) {
                    throw new ConditionsNotMetException(String.format(
                            "Cannot publish the event with id=%d because it's not in the right state %s", eventId, currentState),
                            currentState.toString());
                } else {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            } else if (stateAction == REJECT_EVENT) {
                if (currentState != PENDING) {
                    throw new ConditionsNotMetException(String.format(
                            "Cannot reject the event with id=%d because it's not in the right state %s", eventId, currentState),
                            currentState.toString());
                } else {
                    event.setState(CANCELED);
                }
            }
        }
        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(updatedEvent,
                requestService.getConfirmedRequestsCount(updatedEvent.getId()), getViews(updatedEvent));
    }

    public List<Event> getByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, Integer from, Integer size) {
        Specification<Event> spec = Specification.where(null);
        if (users != null && !users.isEmpty()) {
            spec = spec.and(EventSpecification.initiatorIdIn(users));
        }
        if (states != null && !states.isEmpty()) {
            List<EventState> eventStatesList = new ArrayList<>();
            for (String s: states) {
                try {
                    eventStatesList.add(EventState.valueOf(s));
                } catch (IllegalArgumentException e) {
                    throw new IncorrectDataException(
                            String.format("Incorrect event state %s value.", s), s);
                }
            }
            spec.and(EventSpecification.stateIn(eventStatesList));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecification.categoryIdIn(categories));
        }
        spec = processDates(spec, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAll(spec);
        if (!events.isEmpty()) {
            Map<Long, Long> confirmedRequestCountMap = getConfirmedRequestCountMap(events);
            Map<Long, Long> viewStatsMap = getViewsMap(events);
            int startIndex = from > events.size() ? 0 : from;
            int endIndex = Math.min(startIndex + size, events.size());
            return events.subList(startIndex, endIndex).stream()
                    .map(e -> EventMapper.toEventFullDto(e,
                            confirmedRequestCountMap.getOrDefault(e.getId(), 0L),
                            viewStatsMap.getOrDefault(e.getId(), 0L)))
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         String rangeStart, String rangeEnd, boolean onlyAvailable,
                                         String sort, int from, int size) {
        Specification<Event> spec = Specification.where(null);
        spec.and(EventSpecification.stateIn(List.of(PUBLISHED)));
        if (text != null) {
            spec = spec.and(EventSpecification.annotationOrDescriptionContainsTextIgnoreCase(text));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecification.categoryIdIn(categories));
        }
        if (paid != null) {
            spec = spec.and(EventSpecification.isPaid(paid));
        }
        spec = processDates(spec, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAll(spec);
        if (!events.isEmpty()) {
            Map<Long, Long> confirmedRequestCountMap = getConfirmedRequestCountMap(events);
            Map<Long, Long> viewStatsMap = getViewsMap(events);
            if (onlyAvailable) {
                events.removeIf(e -> confirmedRequestCountMap.get(e.getId()) < e.getParticipantLimit());
            }
            if (sort != null) {
                SortingType sortingType = SortingType.valueOf(sort);
                if (sortingType == EVENT_DATE) {
                    events.sort(Comparator.comparing(Event::getEventDate));
                } else if (sortingType == VIEWS) {
                    events.sort(Comparator.comparing(e -> viewStatsMap.get(e.getId())));
                }
            }
            int startIndex = from > events.size() ? 0 : from;
            int endIndex = Math.min(startIndex + size, events.size());
            return events.subList(startIndex, endIndex).stream()
                    .map(e -> EventMapper.toEventShortDto(e,
                            confirmedRequestCountMap.getOrDefault(e.getId(), 0L),
                            viewStatsMap.getOrDefault(e.getId(), 0L)))
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    private Specification<Event> processDates(Specification<Event> spec, String rangeStart, String rangeEnd) {
        LocalDateTime start = rangeStart == null ? null : DateUtils.convertToDate(rangeStart);
        LocalDateTime end = rangeEnd == null ? null : DateUtils.convertToDate(rangeEnd);
        if (rangeStart == null && rangeEnd == null) {
            spec = spec.and(EventSpecification.startIsGreaterThanOrEqualTo(LocalDateTime.now()));
        } else if (rangeStart != null && rangeEnd != null) {
            if (end.isBefore(start)) {
                throw new IncorrectDataException(
                        String.format("End of period %s is before start.", rangeEnd), rangeEnd);
            }
        }
        if (rangeStart != null) {
            spec = spec.and(EventSpecification.startIsGreaterThanOrEqualTo(start));
        }
        if (rangeEnd != null) {
            spec = spec.and(EventSpecification.endIsLessThanOrEqualTo(end));
        }
        return spec;
    }

    private Map<Long, Long> getViewsMap(Collection<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        LocalDateTime minDate = events
                .stream()
                .map(Event::getCreatedOn)
                .min(Comparator.comparing(Function.identity()))
                .orElseThrow(IllegalStateException::new);
        String[] uris = events.stream().map(e -> "/events/" + e.getId()).toArray(String[]::new);
        return statsClient.getStats(DateUtils.convertToString(minDate),
                        DateUtils.convertToString(LocalDateTime.now()),
                        uris, false)
                .stream()
                .collect(Collectors.toMap(v -> {
                    Path path = Paths.get(v.getUri());
                    return Long.parseLong(String.valueOf(path.getName(path.getNameCount() - 1)));
                }, ViewStats::getHits));
    }

    private Map<Long, Long> getConfirmedRequestCountMap(Collection<Event> events) {
        List<Long> eventIds = events
                .stream()
                .map(Event::getId)
                .toList();
        return requestService
                .getConfirmedRequestsCount(eventIds)
                .stream()
                .collect(Collectors.toMap(RequestCountByEvent::getEventId, RequestCountByEvent::getCount));
    }

    private long getViews(Event event) {
        return statsClient.getStats(DateUtils.convertToString(event.getCreatedOn()),
                DateUtils.convertToString(LocalDateTime.now()),
                new String[]{"/events/" + event.getId()}, true).getFirst().getHits();
    }

    private void userIsAuthorizedCheck(User user, Event event) {
        if (!event.getInitiator().equals(user)) {
            throw new ConditionsNotMetException(
                    String.format("User with id=%d is not an initiator of event with id=%d.", user.getId(), event.getId()),
                    String.valueOf(event.getId()));
        }
    }

    private void validateLocationDto(LocationDto location) {
        if (location.getLat() < -90 || location.getLat() > 90
                || location.getLon() < -180 || location.getLon() > 180) {
            throw new IncorrectDataException(String.format("Wrong coordinates %s format.", location),
                    location.toString());
        }
    }

    private void validateEventDate(String eventDateString, long timeoutInHours) {
        if (DateUtils.convertToDate(eventDateString).isBefore(LocalDateTime.now().plusHours(timeoutInHours))) {
            throw new IncorrectDataException(String.format("Event date %s must be in the future",
                    eventDateString), eventDateString);
        }
    }
}
