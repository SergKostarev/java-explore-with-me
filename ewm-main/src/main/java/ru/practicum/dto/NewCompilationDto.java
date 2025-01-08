package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {

    private final List<@NotNull(message = "Event identifiers list contains null value") Long> events;

    private final Boolean pinned = false;

    @NotBlank(message = "Field title must not be blank.")
    @Size(min = 1, max = 50, message = "Field title should have size between 1 and 50")
    private final String title;
}
