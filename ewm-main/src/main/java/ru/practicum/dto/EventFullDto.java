package ru.practicum.dto;

import lombok.Data;

@Data
public class EventFullDto {
    private final Long id;
    private final String annotation;
    private final CategoryDto category;
    private final Long confirmedRequests;
    private final String eventDate;
    private final String createdOn;
    private final String publishedOn;
    private final String description;
    private final UserShortDto initiator;
    private final LocationDto location;
    private final Boolean paid;
    private final Integer participantLimit;
    private final Boolean requestModeration;
    private final String state;
    private final String title;
    private final Long views;
}
