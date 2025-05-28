package photo_mgmt_backend.model.dto.public_album;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record PublicPhotoDTO(
        String photoName,
        String path,
        ZonedDateTime uploadedAt,
        Boolean isEdited
) { }
