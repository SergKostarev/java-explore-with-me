package ru.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.validation.ValueOfEnum;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Field annotation should have size between 20 and 2000")
    private final String annotation;

    private final Long categoryId;

    @Size(min = 20, max = 7000, message = "Field description should have size between 20 and 7000")
    private final String description;

    private final String eventDate;

    private final LocationDto location;

    private final Boolean paid;

    @Min(0)
    private final Integer participantLimit;

    private final Boolean requestModeration;

    @ValueOfEnum(enumClass = StateAction.class)
    private final String stateAction;

    @Size(min = 3, max = 120, message = "Field title should have size between 3 and 120")
    private final String title;
}
