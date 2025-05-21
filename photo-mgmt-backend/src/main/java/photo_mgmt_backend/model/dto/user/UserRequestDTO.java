package photo_mgmt_backend.model.dto.user;

public record UserRequestDTO(
        String userName,
        String email,
        String password,
        String role
) { }