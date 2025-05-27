package photo_mgmt_backend.service.photo_version;

import photo_mgmt_backend.model.dto.photo_version.PhotoVersionDTO;
import photo_mgmt_backend.model.dto.photo_version.PhotoVersionHistoryDTO;
import photo_mgmt_backend.model.dto.photo_version.RevertToVersionRequestDTO;

import java.util.UUID;

public interface PhotoVersionService {

    /**
     * Get complete version history for a photo
     */
    PhotoVersionHistoryDTO getVersionHistory(UUID photoId, UUID requestingUserId);

    /**
     * Get the original (unedited) image URL
     */
    String getOriginalImageUrl(UUID photoId, UUID requestingUserId);

    /**
     * Revert photo to a specific version
     */
    PhotoVersionDTO revertToVersion(RevertToVersionRequestDTO request, UUID requestingUserId);

    /**
     * Get next version number for a photo
     */
    Integer getNextVersionNumber(UUID photoId);

    /**
     * Generate edit description based on edit parameters
     */
    String generateEditDescription(photo_mgmt_backend.model.entity.PhotoEditEntity editEntity);
}