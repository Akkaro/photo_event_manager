package photo_mgmt_backend.model.dto.photo_edit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PhotoEditRequestDTO(
        @NotNull(message = "Photo ID is required.")
        UUID photoId,
        BigDecimal brightness,
        BigDecimal contrast
) { }

