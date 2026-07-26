package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewUserRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Wrong email format")
    @Size(min = 6, max = 264, message = "Email should be between 6 and 264 characters")
    private String email;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 250, message = "Name should be between 2 and 250 characters")
    private String name;
}
