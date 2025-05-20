package photo_mgmt_backend.model.dto.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlbumRequestDTO(
        @NotBlank(message = "Name is required and cannot be empty.")
        @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters.")
        String albumName
) { }
