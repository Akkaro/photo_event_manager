package photo_mgmt_backend.security.service.auth;

import java.util.UUID;

public interface AuthService {

    boolean isSelf(UUID userId);
}
