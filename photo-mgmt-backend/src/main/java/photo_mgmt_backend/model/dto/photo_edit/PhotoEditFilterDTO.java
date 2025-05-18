package photo_mgmt_backend.model.dto.photo_edit;

import jakarta.validation.constraints.Min;
import photo_mgmt_backend.controller.util.RestUtil;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record PhotoEditFilterDTO(
        UUID photoId,
        UUID ownerId,
        ZonedDateTime editedBefore,
        ZonedDateTime editedAfter,
        @Min(value = 0, message = "Page number must be at least 0.")
        Integer pageNumber,

        @Min(value = 1, message = "Page size must be at least 1.")
        Integer pageSize
) {

    public PhotoEditFilterDTO {
        pageNumber = Objects.requireNonNullElse(pageNumber, RestUtil.DEFAULT_PAGE_NUMBER);
        pageSize = Objects.requireNonNullElse(pageSize, RestUtil.DEFAULT_PAGE_SIZE);
    }
}
