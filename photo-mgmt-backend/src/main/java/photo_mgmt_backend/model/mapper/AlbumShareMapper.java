package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import photo_mgmt_backend.model.dto.album_share.AlbumShareResponseDTO;
import photo_mgmt_backend.model.entity.AlbumShareEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumShareMapper {

    @Mapping(target = "albumName", source = "album.albumName")
    @Mapping(target = "sharedWithUserName", source = "sharedWithUser.userName")
    @Mapping(target = "sharedWithUserEmail", source = "sharedWithUser.email")
    @Mapping(target = "sharedByUserName", source = "sharedByUser.userName")
    @Mapping(target = "sharedByUserEmail", source = "sharedByUser.email")
    AlbumShareResponseDTO convertEntityToResponseDto(AlbumShareEntity entity);

    List<AlbumShareResponseDTO> convertEntitiesToResponseDtos(List<AlbumShareEntity> entities);
}