package photo_mgmt_backend.service.photo;

import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;

import java.util.UUID;

public interface PhotoService {

    CollectionResponseDTO<PhotoResponseDTO> findAll(PhotoFilterDTO filter);

    PhotoResponseDTO findById(UUID id);

    PhotoResponseDTO save(PhotoRequestDTO photoRequestDTO, UUID ownerId);

    PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO, UUID ownerId);

    void delete(UUID id);
}