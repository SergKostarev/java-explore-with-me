package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class RequestController {

    private final RequestService requestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{userId}/requests")
    public ParticipationRequestDto post(@PathVariable Long userId,
                                        @RequestParam Long eventId) {
        return requestService.post(userId, eventId);
    }

    @GetMapping(path = "/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return requestService.getByRequesterId(userId);
    }

    @PatchMapping(path = "/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId,
                                          @PathVariable Long requestId) {
        return requestService.cancel(userId, requestId);
    }

}
