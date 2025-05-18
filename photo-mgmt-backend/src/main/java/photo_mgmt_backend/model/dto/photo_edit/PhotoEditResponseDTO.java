package photo_mgmt_backend.model.dto.photo_edit;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.ZonedDateTime;

public record PhotoEditResponseDTO(
        UUID editId,
        UUID photoId,
        UUID ownerId,
        String ownerName,
        BigDecimal brightness,
        BigDecimal contrast,
        ZonedDateTime editedAt
) { }
