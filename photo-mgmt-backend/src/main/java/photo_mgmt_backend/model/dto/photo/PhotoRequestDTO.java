package photo_mgmt_backend.model.dto.photo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PhotoRequestDTO(
        @Min(value = 0, message = "Album ID must be at least 0.")
        UUID albumId,
        // You might have base64 image data or some other representation
        @NotBlank(message = "Image Path is required and cannot be empty.")
        @Size(min = 1, max = 256, message = "Image path must be between 1 and 256 characters.")
        String imagePath
) { }
