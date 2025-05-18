package photo_mgmt_backend.service.photo_edit;

import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditFilterDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditRequestDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditResponseDTO;

import java.util.UUID;

public interface PhotoEditService {

    CollectionResponseDTO<PhotoEditResponseDTO> findAll(PhotoEditFilterDTO filter);

    PhotoEditResponseDTO findById(UUID id);

    PhotoEditResponseDTO save(PhotoEditRequestDTO photoEditRequestDTO, UUID ownerId);

    PhotoEditResponseDTO update(UUID id, PhotoEditRequestDTO photoEditRequestDTO, UUID ownerId);

    void delete(UUID id);
}