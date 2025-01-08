package ru.practicum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {

    @NotBlank(message = "Field email must not be blank.")
    @Email(message = "Incorrect email format.")
    @Size(min = 6, max = 254, message = "Field email should have size between 6 and 254")
    private final String email;

    @NotBlank(message = "Field name must not be blank.")
    @Size(min = 2, max = 250, message = "Field name should have size between 2 and 250")
    private final String name;

}
