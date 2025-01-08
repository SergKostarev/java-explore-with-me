package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDto {

    private final Long id;

    @NotBlank(message = "Field name must not be blank.")
    @Size(min = 1, max = 50, message = "Field name should have size less than 50")
    private final String name;
}
