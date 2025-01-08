package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateEventController {

    private final EventService eventService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{userId}/events")
    public EventFullDto post(@PathVariable Long userId,
                             @Valid @RequestBody NewEventDto event) {
        return eventService.post(userId, event);
    }

    @GetMapping(path = "/{userId}/events")
    public List<EventShortDto> getEventsByUserId(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @GetMapping(path = "/{userId}/events/{eventId}")
    public EventFullDto getFullEventByUserId(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        return eventService.getFullEventByUserId(userId, eventId);
    }

    @GetMapping(path = "/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping(path = "/{userId}/events/{eventId}")
    public EventFullDto update(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateEvent) {
        return eventService.updateByUser(userId, eventId, updateEvent);
    }

    @PatchMapping(path = "/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                             @PathVariable Long eventId,
                                                             @Valid @RequestBody EventRequestStatusUpdate requestStatusUpdate) {
        return eventService.changeRequestStatus(userId, eventId, requestStatusUpdate);
    }

}
