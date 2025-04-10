package photo_mgmt_backend.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.dto.user.UserRequestDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.model.mapper.UserMapper;
import photo_mgmt_backend.repository.user.UserRepository;
import photo_mgmt_backend.repository.user.UserSpec;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceBean implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSpec userSpec;

    @Override
    public CollectionResponseDTO<UserResponseDTO> findAll(UserFilterDTO filter) {
        Specification<UserEntity> spec = userSpec.createSpecification(filter);
        PageRequest page = PageRequest.of(filter.pageNumber(), filter.pageSize());
        Page<UserEntity> users = userRepository.findAll(spec, page);

        return CollectionResponseDTO.<UserResponseDTO>builder()
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .elements(userMapper.convertEntitiesToResponseDtos(users.getContent()))
                .build();
    }

    @Override
    public UserResponseDTO findById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, id));
        return userMapper.convertEntityToResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO save(UserRequestDTO userRequestDTO) {
        UserEntity user = userMapper.convertRequestDtoToEntity(userRequestDTO);
        UserEntity saved = userRepository.save(user);
        return userMapper.convertEntityToResponseDto(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO update(UUID id, UserRequestDTO userRequestDTO) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, id));

        userMapper.updateUserEntity(existingUser, userRequestDTO);
        UserEntity updated = userRepository.save(existingUser);
        return userMapper.convertEntityToResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, id));

        userRepository.delete(user);
    }
}
