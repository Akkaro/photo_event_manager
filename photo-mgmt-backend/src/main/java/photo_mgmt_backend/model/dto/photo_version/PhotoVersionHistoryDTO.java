package photo_mgmt_backend.model.dto.photo_version;

import java.util.UUID;

public record PhotoVersionHistoryDTO(
        UUID photoId,
        String photoName,
        String originalUrl,
        String currentUrl,
        Integer currentVersion,
        Integer totalVersions,
        java.util.List<PhotoVersionDTO> versions
) { }