package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private final List<@NotNull(message = "Event identifiers list contains null value") Long> events;
    private final Boolean pinned;

    @Size(min = 1, max = 50, message = "Field title should have size between 3 and 120")
    private final String title;
}
