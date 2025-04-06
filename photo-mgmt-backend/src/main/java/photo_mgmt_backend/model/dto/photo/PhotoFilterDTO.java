package photo_mgmt_backend.model.dto.photo;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PhotoFilterDTO(
        UUID albumId,
        UUID ownerId,
        Boolean isEdited,
        ZonedDateTime uploadedBefore,
        ZonedDateTime uploadedAfter,
        Integer pageNumber,
        Integer pageSize
) { }
