package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import photo_mgmt_backend.model.dto.user.UserRequestDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.model.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper extends DtoMapper<UserEntity, UserRequestDTO, UserResponseDTO> {

    @Override
    UserResponseDTO convertEntityToResponseDto(UserEntity entity);

    @Mapping(target = "userId", ignore = true)
    void updateUserEntity(@MappingTarget UserEntity userEntity, UserRequestDTO userRequestDTO);
}
