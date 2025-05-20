package photo_mgmt_backend.service.user;

import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.dto.user.UserRequestDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;

import java.util.UUID;

public interface UserService {

    CollectionResponseDTO<UserResponseDTO> findAll(UserFilterDTO filter);

    UserResponseDTO findById(UUID id);

    UserResponseDTO save(UserRequestDTO userRequestDTO);

    UserResponseDTO update(UUID id, UserRequestDTO userRequestDTO);
    UserResponseDTO getUserInfo(String email);
    void delete(UUID id);
}
