package photo_mgmt_backend.security.service.user;

import en.sd.chefmgmt.exception.model.ExceptionCode;
import en.sd.chefmgmt.model.entity.UserEntity;
import en.sd.chefmgmt.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceBean implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .map(this::getUserDetails)
                .orElseThrow(() -> new BadCredentialsException(ExceptionCode.INVALID_CREDENTIALS.getMessage()));
    }

    private UserDetails getUserDetails(UserEntity user) {
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}