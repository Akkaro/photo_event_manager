package photo_mgmt_backend.model.dto.auth;

import en.sd.chefmgmt.model.entity.Role;

import java.util.UUID;

public record LoginResponseDTO(UUID id, String email, Role role) { }