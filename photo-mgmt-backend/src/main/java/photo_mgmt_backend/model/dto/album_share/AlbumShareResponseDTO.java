// photo-mgmt-backend/src/main/java/photo_mgmt_backend/model/dto/album_share/AlbumShareResponseDTO.java
package photo_mgmt_backend.model.dto.album_share;

import java.time.ZonedDateTime;
import java.util.UUID;

public record AlbumShareResponseDTO(
        UUID albumShareId,
        UUID albumId,
        String albumName,
        UUID sharedWithUserId,
        String sharedWithUserName,
        String sharedWithUserEmail,
        UUID sharedByUserId,
        String sharedByUserName,
        String sharedByUserEmail,
        ZonedDateTime sharedAt
) { }