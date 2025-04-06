package photo_mgmt_backend.model.dto.chef;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ChefResponseDTO(UUID id, String name, String cnp, ZonedDateTime birthDate, double numberOfStars) { }