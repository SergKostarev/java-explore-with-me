package ru.practicum.controller;

import dto.EndpointHit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.SortingType;
import ru.practicum.service.EventService;
import ru.practicum.validation.ValueOfEnum;
import utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController {

    private final EventService eventService;

    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                                    @RequestParam(required = false)
                                                    List<@NotNull(message = "Categories identifiers list contains null value") Long> categories,
                                                    @RequestParam(required = false) Boolean paid,
                                                    @RequestParam(required = false) String rangeStart,
                                                    @RequestParam(required = false) String rangeEnd,
                                                    @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                    @RequestParam(required = false) @ValueOfEnum(enumClass = SortingType.class) String sort,
                                                    @RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    HttpServletRequest request) {
        sendHit(request);
        List<EventShortDto> events = eventService.getEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return events;
    }

    @GetMapping(path = "/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        sendHit(request);
        EventFullDto event = eventService.getEvent(id);
        return event;
    }

    private void sendHit(HttpServletRequest request) {
        statsClient.postHit(new EndpointHit(request.getRequestURI(), request.getRemoteAddr(),
                DateUtils.convertToString(LocalDateTime.now())));
    }

}
