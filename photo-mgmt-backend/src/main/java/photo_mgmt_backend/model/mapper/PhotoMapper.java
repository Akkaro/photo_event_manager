package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
import photo_mgmt_backend.model.entity.PhotoEntity;

@Mapper(componentModel = "spring")
public interface PhotoMapper extends DtoMapper<PhotoEntity, PhotoRequestDTO, PhotoResponseDTO> {

    @Override
    @Mapping(target = "albumName", source = "album.albumName")
    @Mapping(target = "ownerName", source = "owner.userName")
    PhotoResponseDTO convertEntityToResponseDto(PhotoEntity entity);

    //@Mapping(target = "photoId", ignore = true)
    void updatePhotoEntity(@MappingTarget PhotoEntity photoEntity, PhotoRequestDTO photoRequestDTO);
}