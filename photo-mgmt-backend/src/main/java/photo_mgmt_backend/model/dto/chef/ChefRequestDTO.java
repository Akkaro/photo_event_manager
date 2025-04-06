package photo_mgmt_backend.model.dto.chef;

import en.sd.chefmgmt.controller.validator.ValidBirthDate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public record ChefRequestDTO(
        @NotBlank(message = "Name is required and cannot be empty.")
        @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters.")
        String name,

        @Min(value = 0, message = "Rating must be at least 0.")
        @Max(value = 5, message = "Rating must not exceed 5.")
        Double rating,

        @NotBlank(message = "CNP is required and cannot be empty.")
        @Size(min = 13, max = 13, message = "CNP must have 13 characters.")
        String cnp,

        @ValidBirthDate(message = "Birthdate must a valid date be in the past and at least 18 years old.")
        ZonedDateTime birthDate
) { }
