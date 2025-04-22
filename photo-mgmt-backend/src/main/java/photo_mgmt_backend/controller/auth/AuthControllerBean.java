package photo_mgmt_backend.controller.auth;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import photo_mgmt_backend.model.dto.auth.LoginRequestDTO;
import photo_mgmt_backend.model.dto.auth.RegisterRequestDTO;
import photo_mgmt_backend.model.dto.auth.RegisterResponseDTO;

@Slf4j
@RestController
public class AuthControllerBean implements AuthController {

    @Override
    public void login(LoginRequestDTO loginRequestDTO) {
        log.info("[AUTH] User {} logged in", loginRequestDTO.email());
    }

    @Override
    public void register(RegisterRequestDTO registerRequestDTO) {
        log.info("[AUTH] User {} registered", registerRequestDTO.email());
    }
}
