package photo_mgmt_backend.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.model.mapper.UserMapper;
import photo_mgmt_backend.security.service.auth.AuthService;
import photo_mgmt_backend.service.user.UserService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserControllerBean implements UserController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO getUserInfo() {
        String email = authService.getLoggedUser();
        log.info("[USER] Getting user info for {}", email);

        return userService.getUserInfo(email);
    }
}
