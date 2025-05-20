package photo_mgmt_backend.model.dto.album;

import jakarta.validation.constraints.Min;
import photo_mgmt_backend.controller.util.RestUtil;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record AlbumFilterDTO(
        String albumName,
        UUID ownerId,
        ZonedDateTime createdAt,
        @Min(value = 0, message = "Page number must be at least 0.")
        Integer pageNumber,
        @Min(value = 1, message = "Page size must be at least 1.")
        Integer pageSize
) {

    public AlbumFilterDTO {
        pageNumber = Objects.requireNonNullElse(pageNumber, RestUtil.DEFAULT_PAGE_NUMBER);
        pageSize = Objects.requireNonNullElse(pageSize, RestUtil.DEFAULT_PAGE_SIZE);
    }
}
