package photo_mgmt_backend.model.dto.photo;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record PhotoRequestDTO(
        @NotNull(message = "Album ID is required and cannot be empty.")
        UUID albumId,
        @NotBlank(message = "Image Name is required and cannot be empty.")
        @Size(min = 1, max = 256, message = "Image path must be between 1 and 256 characters.")
        String photoName
) { }
