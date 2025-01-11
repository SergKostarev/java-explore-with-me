package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateUserController {

    private final UserService userService;

    @PatchMapping(path = "/{userId}/subscriptions")
    public UserDto updateSubscription(@PathVariable Long userId,
                                     @RequestParam Boolean allow) {
        return userService.updateSubscription(userId, allow);
    }

    @PatchMapping(path = "/{userId}/subscriptions/{subscriptionId}")
    public UserDto subscribe(@PathVariable Long userId,
                                      @PathVariable Long subscriptionId) {
        return userService.subscribe(userId, subscriptionId);
    }

    @DeleteMapping(path = "/{userId}/subscriptions/{subscriptionId}")
    public UserDto unsubscribe(@PathVariable Long userId,
                             @PathVariable Long subscriptionId) {
        return userService.unsubscribe(userId, subscriptionId);
    }

    @DeleteMapping(path = "/{userId}/subscriptions")
    public UserDto deleteAllSubscriptions(@PathVariable Long userId) {
        return userService.deleteAllSubscriptions(userId);
    }

    @GetMapping(path = "/{userId}/subscriptions")
    public List<UserShortDto> getSubscriptions(@PathVariable Long userId) {
        return userService.getSubscriptions(userId);
    }

    @GetMapping(path = "/{userId}/subscriptions/events")
    public List<EventShortDto> getSubscriptionEvents(@PathVariable Long userId) {
        return userService.getSubscriptionEvents(userId);
    }

}
