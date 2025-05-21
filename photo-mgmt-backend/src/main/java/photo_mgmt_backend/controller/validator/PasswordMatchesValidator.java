package photo_mgmt_backend.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import photo_mgmt_backend.model.dto.auth.RegisterRequestDTO;

@Component
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequestDTO> {

    @Override
    public boolean isValid(RegisterRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.password() == null || dto.confirmPassword() == null) {
            return false;
        }
        return dto.password().equals(dto.confirmPassword());
    }
}