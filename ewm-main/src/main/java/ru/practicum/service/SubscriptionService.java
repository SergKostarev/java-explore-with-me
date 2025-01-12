package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.IncorrectDataException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final UserService userService;

    private final EventService eventService;

    @Transactional
    public UserDto updateSubscription(long userId, boolean allow) {
        User user = userService.getById(userId);
        if (user.getAllowSubscription() && !allow
                || !user.getAllowSubscription() && allow) {
            user.setAllowSubscription(allow);
        }
        return UserMapper.toUserDto(userService.saveUser(user));
    }

    @Transactional
    public UserDto subscribe(long userId, long subscriptionId) {
        User user = userService.getById(userId);
        User subscription = userService.getById(subscriptionId);
        Set<User> subscriptions = user.getSubscriptions();
        if (user.equals(subscription) || subscriptions.contains(subscription)) {
            throw new IncorrectDataException(
                    String.format("Cannot subscribe on user with id=%d.",
                            subscriptionId), String.valueOf(subscriptionId));
        }
        if (!subscription.getAllowSubscription()) {
            throw new ConditionsNotMetException(
                    String.format("Subscription on user with id=%d is not allowed.",
                            subscriptionId), String.valueOf(subscriptionId));
        }
        subscriptions.add(subscription);
        return UserMapper.toUserDto(userService.saveUser(user));
    }

    @Transactional
    public UserDto unsubscribe(long userId, long subscriptionId) {
        User user = userService.getById(userId);
        User subscription = userService.getById(subscriptionId);
        user.getSubscriptions().remove(subscription);
        return UserMapper.toUserDto(userService.saveUser(user));
    }

    @Transactional
    public UserDto deleteAllSubscriptions(Long userId) {
        User user = userService.getById(userId);
        user.getSubscriptions().clear();
        return UserMapper.toUserDto(userService.saveUser(user));
    }

    public List<UserShortDto> getSubscriptions(Long userId) {
        return userService.getById(userId)
                .getSubscriptions()
                .stream()
                .map(UserMapper::toUserShortDto)
                .toList();
    }

    public List<EventShortDto> getSubscriptionEvents(Long userId) {
        return eventService.getPublishedEventsDtoByUserId(
                userService.getById(userId)
                .getSubscriptions()
                .stream()
                .map(User::getId)
                .toList());
    }
}
