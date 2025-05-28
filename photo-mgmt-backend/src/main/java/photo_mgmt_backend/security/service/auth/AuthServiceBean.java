package photo_mgmt_backend.security.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.repository.user.UserRepository;

import java.util.UUID;

@Slf4j
@Service("authService")
@RequiredArgsConstructor
public class AuthServiceBean implements AuthService {

    private final UserRepository userRepository;

    @Override
    public String getLoggedUser() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public boolean isOwner(UUID entityOwnerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("[AUTH] Checking if {} is owner of entity with ID {}", auth.getName(), entityOwnerId);

        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadCredentialsException(ExceptionCode.FORBIDDEN_ACCESS.getMessage()));

        boolean isOwner = user.getUserId().equals(entityOwnerId);
        log.debug("[AUTH] User {} {} owner of entity with ID {}",
                auth.getName(), isOwner ? "is" : "is not", entityOwnerId);

        return isOwner;
    }

    @Override
    public boolean isSelf(UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("[AUTH] Checking if {} is the same as user with ID {}", auth.getName(), userId);

        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadCredentialsException(ExceptionCode.FORBIDDEN_ACCESS.getMessage()));

        boolean isSelf = user.getUserId().equals(userId);
        log.debug("[AUTH] User {} {} the same as user with ID {}",
                auth.getName(), isSelf ? "is" : "is not", userId);

        return isSelf;
    }
}