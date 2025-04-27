package photo_mgmt_backend.model.dto.user;

import jakarta.validation.constraints.Min;
import photo_mgmt_backend.controller.util.RestUtil;

import java.time.ZonedDateTime;
import java.util.Objects;

public record UserFilterDTO(
        String userName,
        String email,
        String role,
        ZonedDateTime createdAt,
        @Min(value = 0, message = "Page number must be at least 0.")
        Integer pageNumber,
        @Min(value = 1, message = "Page size must be at least 1.")
        Integer pageSize
) {



    public UserFilterDTO {
        pageNumber = Objects.requireNonNullElse(pageNumber, RestUtil.DEFAULT_PAGE_NUMBER);
        pageSize = Objects.requireNonNullElse(pageSize, RestUtil.DEFAULT_PAGE_SIZE);
    }
}
