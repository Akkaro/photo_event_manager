package photo_mgmt_backend.model.dto.album;

import java.time.ZonedDateTime;
import java.util.UUID;

public record AlbumResponseDTO(
        UUID albumId,
        String albumName,
        UUID ownerId,
        String ownerName,
        String qrCode,
        ZonedDateTime createdAt,
        Boolean isPublic,
        String publicToken,
        String publicUrl
) { }