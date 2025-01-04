package ru.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewEventDto {

    @NotNull(message = "Field annotation must not be null.")
    @NotBlank(message = "Field annotation must not be blank.")
    @Size(min = 20, max = 2000, message = "Field annotation should have size between 20 and 2000")
    private final String annotation;

    @NotNull
    private final Long category;

    @NotNull(message = "Field description must not be null.")
    @NotBlank(message = "Field description must not be blank.")
    @Size(min = 20, max = 7000, message = "Field description should have size between 20 and 7000")
    private final String description;

    @NotNull(message = "Field eventDate must not be null.")
    private final String eventDate;

    @NotNull
    private final LocationDto location;

    private final Boolean paid = false;

    @Min(0)
    private final Integer participantLimit = 0;

    private final Boolean requestModeration = true;

    @NotNull(message = "Field title must not be null.")
    @NotBlank(message = "Field title must not be blank.")
    @Size(min = 3, max = 120, message = "Field title should have size between 3 and 120")
    private final String title;
}
