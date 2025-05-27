package photo_mgmt_backend.model.dto.photo_version;

import java.util.UUID;

public record RevertToVersionRequestDTO(
        UUID photoId,
        Integer targetVersion,
        String reason
) { }
