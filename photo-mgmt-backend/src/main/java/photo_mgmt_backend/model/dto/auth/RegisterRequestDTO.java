package photo_mgmt_backend.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import photo_mgmt_backend.controller.validator.PasswordMatches;

@PasswordMatches
public record RegisterRequestDTO(
        @NotBlank(message = "Username is required and cannot be empty.")
        String userName,
        @NotBlank(message = "Email is required and cannot be empty.")
        @Email(message = "Email must be a valid email address.")
        String email,
        @NotBlank(message = "Password is required and cannot be empty.")
        String password,
        @NotBlank(message = "Password confirmation is required and cannot be empty.")
        String confirmPassword
) { }
