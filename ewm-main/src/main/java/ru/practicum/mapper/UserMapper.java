package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

import java.util.List;

@UtilityClass
public class UserMapper {

    public static User toUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setEmail(newUserRequest.getEmail());
        return user;
    }

    public static UserDto toUserDto(User user) {
        List<UserShortDto> subscriptions = user.getSubscriptions()
                .stream()
                .map(UserMapper::toUserShortDto)
                .toList();
        return new UserDto(user.getId(), user.getEmail(),
                user.getName(), user.getAllowSubscription(), subscriptions);
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

}
