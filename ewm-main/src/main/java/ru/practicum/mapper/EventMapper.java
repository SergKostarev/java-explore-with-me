package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.LocationDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.*;
import utils.DateUtils;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public static Location toLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }

    public static Event fromNewEventDtoToEvent(NewEventDto newEventDto, Category category,
                                               User user, Location location) {
        Event event = new Event();
        event.setCategory(category);
        event.setInitiator(user);
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(DateUtils.convertToDate(newEventDto.getEventDate()));
        event.setLocation(location);
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setTitle(newEventDto.getTitle());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        return event;
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                DateUtils.convertToString(event.getEventDate()),
                event.getCreatedOn() == null ? null : DateUtils.convertToString(event.getCreatedOn()),
                event.getPublishedOn() == null ? null : DateUtils.convertToString(event.getPublishedOn()),
                event.getDescription(),
                UserMapper.toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getState().name(),
                event.getTitle(),
                views
        );
    }

    public static EventShortDto toEventShortDto(Event savedEvent, Long confirmedRequests, Long views) {
        return new EventShortDto(
                savedEvent.getId(),
                savedEvent.getAnnotation(),
                CategoryMapper.toCategoryDto(savedEvent.getCategory()),
                confirmedRequests,
                DateUtils.convertToString(savedEvent.getEventDate()),
                UserMapper.toUserShortDto(savedEvent.getInitiator()),
                savedEvent.getPaid(),
                savedEvent.getTitle(),
                views
        );
    }
}
