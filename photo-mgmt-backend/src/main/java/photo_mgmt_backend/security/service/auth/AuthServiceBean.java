package photo_mgmt_backend.security.service.auth;

import en.sd.chefmgmt.exception.model.ExceptionCode;
import en.sd.chefmgmt.model.entity.UserEntity;
import en.sd.chefmgmt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authService")
@RequiredArgsConstructor
public class AuthServiceBean implements AuthService {

    private final UserRepository userRepository;

    @Override
    public boolean isSelf(UUID userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new BadCredentialsException(ExceptionCode.FORBIDDEN_ACCESS.getMessage()));

        return user.getId().equals(userId);
    }
}
