package photo_mgmt_backend.security.service.auth;

import java.util.UUID;

public interface AuthService {

    String getLoggedUser();

    boolean isOwner(UUID entityOwnerId);

    boolean isSelf(UUID userId);
}