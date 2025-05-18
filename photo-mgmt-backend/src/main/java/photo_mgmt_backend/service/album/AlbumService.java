package photo_mgmt_backend.service.album;

import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;

import java.util.UUID;

public interface AlbumService {

    CollectionResponseDTO<AlbumResponseDTO> findAll(AlbumFilterDTO filter);

    AlbumResponseDTO findById(UUID id);

    AlbumResponseDTO save(AlbumRequestDTO albumRequestDTO, UUID ownerId);

    AlbumResponseDTO update(UUID id, AlbumRequestDTO albumRequestDTO, UUID ownerId);

    void delete(UUID id);
}