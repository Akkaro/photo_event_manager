package photo_mgmt_backend.model.dto.user;

import java.time.ZonedDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String userName,
        String email,
        String role,
        ZonedDateTime createdAt
) { }