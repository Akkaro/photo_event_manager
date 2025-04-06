package photo_mgmt_backend.model.dto.chef;

import en.sd.chefmgmt.controller.util.RestUtil;
import jakarta.validation.constraints.Min;

import java.time.ZonedDateTime;
import java.util.Objects;

public record ChefFilterDTO(
        String name,
        Double rating,
        String cnp,
        ZonedDateTime birthDate,

        @Min(value = 0, message = "Page number must be at least 0.")
        Integer pageNumber,

        @Min(value = 1, message = "Page size must be at least 1.")
        Integer pageSize
) {

    public ChefFilterDTO {
        pageNumber = Objects.requireNonNullElse(pageNumber, RestUtil.DEFAULT_PAGE_NUMBER);
        pageSize = Objects.requireNonNullElse(pageSize, RestUtil.DEFAULT_PAGE_SIZE);
    }
}