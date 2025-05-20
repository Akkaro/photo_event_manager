package photo_mgmt_backend.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, ZonedDateTime> {

    private static final Integer LEGAL_AGE = 18;

    @Override
    public boolean isValid(ZonedDateTime birthDate, ConstraintValidatorContext context) {
        return birthDate != null && ChronoUnit.YEARS.between(ZonedDateTime.now(), birthDate) <= -LEGAL_AGE;
    }
}