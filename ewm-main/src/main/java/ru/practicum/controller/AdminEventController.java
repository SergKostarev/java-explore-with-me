package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @PatchMapping(path = "{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventAdminRequest updateEvent) {
        return eventService.updateByAdmin(eventId, updateEvent);
    }

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false)
                                            List<@NotNull(message = "Users identifiers list contains null value") Long> users,
                                            @RequestParam(required = false)
                                            List<@NotNull(message = "States list contains null value") String> states,
                                            @RequestParam(required = false)
                                            List<@NotNull(message = "Categories identifiers list contains null value") Long> categories,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}
