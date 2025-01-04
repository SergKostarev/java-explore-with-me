package ru.practicum.dto;

import lombok.Data;

@Data
public class LocationDto { //TODO validate
    private final Float lat;
    private final Float lon;
}