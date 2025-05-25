package photo_mgmt_backend.service.album_share;

import photo_mgmt_backend.model.dto.album_share.AlbumShareResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AlbumShareService {
    void shareAlbum(UUID albumId, String userEmail, UUID sharedByUserId);
    void unshareAlbum(UUID albumId, String userEmail, UUID currentUserId);
    List<AlbumShareResponseDTO> getAlbumShares(UUID albumId, UUID currentUserId);
    boolean isAlbumSharedWithUser(UUID albumId, UUID userId);
}