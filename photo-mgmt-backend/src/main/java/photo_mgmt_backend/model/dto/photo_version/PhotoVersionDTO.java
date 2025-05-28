package photo_mgmt_backend.model.dto.photo_version;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PhotoVersionDTO(
        UUID editId,
        Integer versionNumber,
        String imageUrl,
        String editDescription,
        ZonedDateTime createdAt,
        String ownerName,
        Boolean isCurrent
) { }
