package photo_mgmt_backend.model.dto.album_share;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UnshareAlbumRequestDTO(
        @NotBlank(message = "User email is required")
        @Email(message = "Must be a valid email address")
        String userEmail
) { }