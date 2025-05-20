package photo_mgmt_backend.model.dto.photo;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PhotoResponseDTO(
        UUID photoId,
        String photoName,
        UUID albumId,
        UUID ownerId,
        String path,
        ZonedDateTime uploadedAt,
        Boolean isEdited
) { }
