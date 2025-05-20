package photo_mgmt_backend.model.dto.auth;

import photo_mgmt_backend.model.entity.Role;

import java.util.UUID;

public record RegisterResponseDTO(UUID id, String email, Role role) { }