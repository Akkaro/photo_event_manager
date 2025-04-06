package photo_mgmt_backend.model.dto.photo_edit;

import java.math.BigDecimal;
import java.util.UUID;

public record PhotoEditRequestDTO(
        UUID photoId,
        BigDecimal brightness,
        BigDecimal contrast
) { }

