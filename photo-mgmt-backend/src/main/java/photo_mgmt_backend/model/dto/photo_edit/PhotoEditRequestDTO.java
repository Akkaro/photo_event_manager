package photo_mgmt_backend.model.dto.photo_edit;

import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public record PhotoEditRequestDTO(
        @Min(value = 0, message = "Photo ID must be at least 0.")
        UUID photoId,
        BigDecimal brightness,
        BigDecimal contrast
) { }

