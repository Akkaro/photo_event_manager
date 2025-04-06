package photo_mgmt_backend.model.dto.album;

import java.util.UUID;

public record AlbumFilterDTO(
        String albumName,
        UUID ownerId,
        Integer pageNumber,
        Integer pageSize
) { }
