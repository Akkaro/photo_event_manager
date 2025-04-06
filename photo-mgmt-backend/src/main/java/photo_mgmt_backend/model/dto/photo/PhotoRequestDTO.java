package photo_mgmt_backend.model.dto.photo;

import java.util.UUID;

public record PhotoRequestDTO(
        UUID albumId,
        // You might have base64 image data or some other representation
        String imageData
) { }
