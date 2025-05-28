package photo_mgmt_backend.model.dto.public_album;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record PublicAlbumResponseDTO(
        String albumName,
        String ownerName,
        ZonedDateTime createdAt,
        Integer photoCount,
        List<PublicPhotoDTO> photos
) { }
