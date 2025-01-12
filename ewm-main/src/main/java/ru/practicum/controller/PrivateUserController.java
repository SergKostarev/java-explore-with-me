package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.service.SubscriptionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateUserController {

    private final SubscriptionService subscriptionService;

    @PatchMapping(path = "/{userId}/subscriptions")
    public UserDto updateSubscription(@PathVariable Long userId,
                                     @RequestParam Boolean allow) {
        return subscriptionService.updateSubscription(userId, allow);
    }

    @PatchMapping(path = "/{userId}/subscriptions/{subscriptionId}")
    public UserDto subscribe(@PathVariable Long userId,
                                      @PathVariable Long subscriptionId) {
        return subscriptionService.subscribe(userId, subscriptionId);
    }

    @DeleteMapping(path = "/{userId}/subscriptions/{subscriptionId}")
    public UserDto unsubscribe(@PathVariable Long userId,
                             @PathVariable Long subscriptionId) {
        return subscriptionService.unsubscribe(userId, subscriptionId);
    }

    @DeleteMapping(path = "/{userId}/subscriptions")
    public UserDto deleteAllSubscriptions(@PathVariable Long userId) {
        return subscriptionService.deleteAllSubscriptions(userId);
    }

    @GetMapping(path = "/{userId}/subscriptions")
    public List<UserShortDto> getSubscriptions(@PathVariable Long userId) {
        return subscriptionService.getSubscriptions(userId);
    }

    @GetMapping(path = "/{userId}/subscriptions/events")
    public List<EventShortDto> getSubscriptionEvents(@PathVariable Long userId) {
        return subscriptionService.getSubscriptionEvents(userId);
    }

}
