package photo_mgmt_backend.model.dto.auth;

public record RegisterRequestDTO(
        String userName,
        String email,
        String password
) { }