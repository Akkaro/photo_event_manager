package photo_mgmt_backend.model.dto.photo;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PhotoResponseDTO(
        UUID photoId,
        UUID albumId,
        String albumName,
        UUID ownerId,
        String ownerName,
        String path,
        ZonedDateTime uploadedAt,
        Boolean isEdited
) { }
