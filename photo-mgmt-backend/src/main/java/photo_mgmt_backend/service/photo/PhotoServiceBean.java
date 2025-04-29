package photo_mgmt_backend.service.photo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.model.mapper.PhotoMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.photo.PhotoSpec;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoServiceBean implements PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final PhotoSpec photoSpec;
    private final PhotoMapper photoMapper;

    @Override
    public CollectionResponseDTO<PhotoResponseDTO> findAll(PhotoFilterDTO filter) {
        Specification<PhotoEntity> spec = photoSpec.createSpecification(filter);
        PageRequest page = PageRequest.of(filter.pageNumber(), filter.pageSize());
        Page<PhotoEntity> photos = photoRepository.findAll(spec, page);

        return CollectionResponseDTO.<PhotoResponseDTO>builder()
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalPages(photos.getTotalPages())
                .totalElements(photos.getTotalElements())
                .elements(photoMapper.convertEntitiesToResponseDtos(photos.getContent()))
                .build();
    }

    @Override
    public PhotoResponseDTO findById(UUID id) {
        PhotoEntity photoEntity = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        return photoMapper.convertEntityToResponseDto(photoEntity);
    }

    @Override
    @Transactional
    public PhotoResponseDTO save(PhotoRequestDTO photoRequestDTO, UUID ownerId) {
        // Verify album exists
        AlbumEntity albumEntity = albumRepository.findById(photoRequestDTO.albumId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));

        PhotoEntity photoToBeAdded = photoMapper.convertRequestDtoToEntity(photoRequestDTO);
        photoToBeAdded.setPhotoName(photoRequestDTO.photoName());
        photoToBeAdded.setOwnerId(ownerId);
        photoToBeAdded.setUploadedAt(ZonedDateTime.now());
        photoToBeAdded.setIsEdited(false);

        // In a real application, this would handle file storage
        String path = saveImage(photoRequestDTO.photoName(), ownerId);
        photoToBeAdded.setPath(path);

        PhotoEntity photoAdded = photoRepository.save(photoToBeAdded);

        return photoMapper.convertEntityToResponseDto(photoAdded);
    }

    @Override
    @Transactional
    public PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO, UUID ownerId) {
        PhotoEntity existingPhoto = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        // Verify ownership
        if (!existingPhoto.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
        }

        // Verify album exists if being changed
        if (!existingPhoto.getAlbumId().equals(photoRequestDTO.albumId())) {
            albumRepository.findById(photoRequestDTO.albumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));
        }

        photoMapper.updatePhotoEntity(existingPhoto, photoRequestDTO);
        PhotoEntity photoEntitySaved = photoRepository.save(existingPhoto);

        return photoMapper.convertEntityToResponseDto(photoEntitySaved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PhotoEntity photoEntity = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        photoRepository.deleteById(photoEntity.getPhotoId());

        // In a real application, this would delete the image file
        deleteImageFile(photoEntity.getPath());
    }

    // Helper method for file storage
    private String saveImage(String photoName, UUID ownerId) {
        // In a real implementation, this would save the image to storage
        // For now, just returning a placeholder path
        return "/images/" + ownerId + "/" + UUID.randomUUID() + "-" + photoName + ".jpg";
    }

    // Helper method for file deletion
    private void deleteImageFile(String path) {
        // In a real implementation, this would delete the image file
        // This is a placeholder method
    }
}