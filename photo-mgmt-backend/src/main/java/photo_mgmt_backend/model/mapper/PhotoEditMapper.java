package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditRequestDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditResponseDTO;
import photo_mgmt_backend.model.entity.PhotoEditEntity;

@Mapper(componentModel = "spring")
public interface PhotoEditMapper extends DtoMapper<PhotoEditEntity, PhotoEditRequestDTO, PhotoEditResponseDTO> {

    @Override
    @Mapping(target = "ownerName", source = "owner.userName")
    PhotoEditResponseDTO convertEntityToResponseDto(PhotoEditEntity entity);

    @Mapping(target = "editId", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updatePhotoEditEntity(@MappingTarget PhotoEditEntity photoEditEntity, PhotoEditRequestDTO photoEditRequestDTO);
}
