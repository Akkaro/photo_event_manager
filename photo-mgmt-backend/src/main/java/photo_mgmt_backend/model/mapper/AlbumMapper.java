package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;

@Mapper(componentModel = "spring")
public interface AlbumMapper extends DtoMapper<AlbumEntity, AlbumRequestDTO, AlbumResponseDTO> {

    @Override
    @Mapping(target = "ownerName", source = "owner.userName")
    AlbumResponseDTO convertEntityToResponseDto(AlbumEntity entity);

//    @Override
//    @Mapping(target = "albumId", ignore = true)
//    AlbumResponseDTO convertRequestDtoToEntity(AlbumRequestDTO entity);

    @Mapping(target = "albumId", ignore = true)
    void updateAlbumEntity(@MappingTarget AlbumEntity albumEntity, AlbumRequestDTO albumRequestDTO);
}

