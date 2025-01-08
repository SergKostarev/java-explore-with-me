package ru.practicum.dto;

import lombok.Data;

@Data
public class ApiError {
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;
}
