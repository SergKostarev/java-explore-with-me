package ru.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private final Long id;
    private final String email;
    private final String name;
    private final Boolean allowSubscription;
    private final List<UserShortDto> subscriptions;
}
