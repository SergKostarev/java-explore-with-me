package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.IncorrectDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private UserRepository userRepository;

    private EventService eventService;

    @Autowired
    public UserService(@Lazy EventService eventService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional
    public UserDto post(NewUserRequest newUserRequest) {
        String email = newUserRequest.getEmail();
        boolean emailExists = userRepository.findByEmailIgnoreCase(email).isPresent();
        if (emailExists) {
            throw new DataIntegrityException(String.format("Email %s already exists.", email), email);
        }
        User savedUser = userRepository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public void delete(long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    public List<UserDto> get(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.get(from, size);
        } else {
            users = userRepository.get(ids);
        }
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public User getById(long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found.", id), id);
        }
        return user.get();
    }

    @Transactional
    public UserDto updateSubscription(long userId, boolean allow) {
        User user = getById(userId);
        if (user.getAllowSubscription() && !allow
                || !user.getAllowSubscription() && allow) {
            user.setAllowSubscription(allow);
        }
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public UserDto subscribe(long userId, long subscriptionId) {
        User user = getById(userId);
        User subscription = getById(subscriptionId);
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
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public UserDto unsubscribe(long userId, long subscriptionId) {
        User user = getById(userId);
        User subscription = getById(subscriptionId);
        user.getSubscriptions().remove(subscription);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public UserDto deleteAllSubscriptions(Long userId) {
        User user = getById(userId);
        user.getSubscriptions().clear();
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    public List<UserShortDto> getSubscriptions(Long userId) {
        return getById(userId)
                .getSubscriptions()
                .stream()
                .map(UserMapper::toUserShortDto)
                .toList();
    }

    public List<EventShortDto> getSubscriptionEvents(Long userId) {
        return eventService.getPublishedEventsDtoByUserId(getById(userId)
                .getSubscriptions()
                .stream()
                .map(User::getId)
                .toList());
    }
}
