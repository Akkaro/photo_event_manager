package photo_mgmt_backend.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Email is required and cannot be empty.")
        @Email(message = "Email must be a valid email address.")
        String email,

        @NotBlank(message = "Password is required and cannot be empty.")
        String password
) { }
