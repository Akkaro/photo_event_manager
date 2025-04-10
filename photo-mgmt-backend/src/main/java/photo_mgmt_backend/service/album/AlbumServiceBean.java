package photo_mgmt_backend.service.album;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.DuplicateDataException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.mapper.AlbumMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.album.AlbumSpec;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumServiceBean implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumSpec albumSpec;
    private final AlbumMapper albumMapper;

    @Override
    public CollectionResponseDTO<AlbumResponseDTO> findAll(AlbumFilterDTO filter) {
        Specification<AlbumEntity> spec = albumSpec.createSpecification(filter);
        PageRequest page = PageRequest.of(filter.pageNumber(), filter.pageSize());
        Page<AlbumEntity> albums = albumRepository.findAll(spec, page);

        return CollectionResponseDTO.<AlbumResponseDTO>builder()
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalPages(albums.getTotalPages())
                .totalElements(albums.getTotalElements())
                .elements(albumMapper.convertEntitiesToResponseDtos(albums.getContent()))
                .build();
    }

    @Override
    public AlbumResponseDTO findById(UUID id) {
        AlbumEntity albumEntity = albumRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id));

        return albumMapper.convertEntityToResponseDto(albumEntity);
    }

    @Override
    @Transactional
    public AlbumResponseDTO save(AlbumRequestDTO albumRequestDTO, UUID ownerId) {
        if (albumRepository.existsByAlbumNameAndOwnerId(albumRequestDTO.albumName(), ownerId)) {
            throw new DuplicateDataException(ExceptionCode.ALBUM_NAME_TAKEN, albumRequestDTO.albumName());
        }

        AlbumEntity albumToBeAdded = albumMapper.convertRequestDtoToEntity(albumRequestDTO);
        albumToBeAdded.setOwnerId(ownerId);
        albumToBeAdded.setCreatedAt(ZonedDateTime.now());

        // Generate QR code
        String qrCode = generateQrCode(albumToBeAdded);
        albumToBeAdded.setQrCode(qrCode);

        AlbumEntity albumAdded = albumRepository.save(albumToBeAdded);

        return albumMapper.convertEntityToResponseDto(albumAdded);
    }

    @Override
    @Transactional
    public AlbumResponseDTO update(UUID id, AlbumRequestDTO albumRequestDTO, UUID ownerId) {
        AlbumEntity existingAlbum = albumRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id));

        // Verify ownership
        if (!existingAlbum.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id);
        }

        if (albumRepository.existsByAlbumNameAndOwnerIdAndAlbumIdIsNot(albumRequestDTO.albumName(), ownerId, id)) {
            throw new DuplicateDataException(ExceptionCode.ALBUM_NAME_TAKEN, albumRequestDTO.albumName());
        }

        albumMapper.updateAlbumEntity(existingAlbum, albumRequestDTO);
        AlbumEntity albumEntitySaved = albumRepository.save(existingAlbum);

        return albumMapper.convertEntityToResponseDto(albumEntitySaved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        AlbumEntity albumEntity = albumRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id));

        albumRepository.deleteById(albumEntity.getAlbumId());
    }

    // Helper method for QR code generation
    private String generateQrCode(AlbumEntity album) {
        // In a real implementation, this would generate an actual QR code
        // For now, just returning a placeholder string
        return "qr_code_" + album.getAlbumId().toString();
    }
}