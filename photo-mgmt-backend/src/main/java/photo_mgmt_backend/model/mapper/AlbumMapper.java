package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.service.qr.QRCodeService;

@Mapper(componentModel = "spring")
public abstract class AlbumMapper implements DtoMapper<AlbumEntity, AlbumRequestDTO, AlbumResponseDTO> {

    @Autowired
    protected QRCodeService qrCodeService;

    @Override
    @Mapping(target = "ownerName", source = "owner.userName")
    @Mapping(target = "publicUrl", expression = "java(generatePublicUrl(entity))")
    public abstract AlbumResponseDTO convertEntityToResponseDto(AlbumEntity entity);

    @Mapping(target = "albumId", ignore = true)
    @Mapping(target = "isPublic", ignore = true)
    @Mapping(target = "publicToken", ignore = true)
    public abstract void updateAlbumEntity(@MappingTarget AlbumEntity albumEntity, AlbumRequestDTO albumRequestDTO);

    protected String generatePublicUrl(AlbumEntity entity) {
        if (entity.getPublicToken() != null && Boolean.TRUE.equals(entity.getIsPublic())) {
            return qrCodeService.generatePublicAlbumUrl(entity.getPublicToken());
        }
        return null;
    }
}