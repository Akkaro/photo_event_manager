package photo_mgmt_backend.model.dto.photo_edit;

import java.time.ZonedDateTime;
import java.util.UUID;

public record PhotoEditFilterDTO(
        UUID photoId,
        UUID ownerId,
        ZonedDateTime editedBefore,
        ZonedDateTime editedAfter,
        Integer pageNumber,
        Integer pageSize
) { }
