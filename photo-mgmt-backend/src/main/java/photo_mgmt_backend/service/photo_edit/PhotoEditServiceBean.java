package photo_mgmt_backend.service.photo_edit;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditFilterDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditRequestDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditResponseDTO;
import photo_mgmt_backend.model.entity.PhotoEditEntity;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.model.mapper.PhotoEditMapper;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.photo_edit.PhotoEditRepository;
import photo_mgmt_backend.repository.photo_edit.PhotoEditSpec;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoEditServiceBean implements PhotoEditService {

    private final PhotoEditRepository photoEditRepository;
    private final PhotoRepository photoRepository;
    private final PhotoEditSpec photoEditSpec;
    private final PhotoEditMapper photoEditMapper;

    @Override
    public CollectionResponseDTO<PhotoEditResponseDTO> findAll(PhotoEditFilterDTO filter) {
        Specification<PhotoEditEntity> spec = photoEditSpec.createSpecification(filter);
        PageRequest page = PageRequest.of(filter.pageNumber(), filter.pageSize());
        Page<PhotoEditEntity> photoEdits = photoEditRepository.findAll(spec, page);

        return CollectionResponseDTO.<PhotoEditResponseDTO>builder()
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalPages(photoEdits.getTotalPages())
                .totalElements(photoEdits.getTotalElements())
                .elements(photoEditMapper.convertEntitiesToResponseDtos(photoEdits.getContent()))
                .build();
    }

    @Override
    public PhotoEditResponseDTO findById(UUID id) {
        PhotoEditEntity photoEditEntity = photoEditRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id));

        return photoEditMapper.convertEntityToResponseDto(photoEditEntity);
    }

    @Override
    @Transactional
    public PhotoEditResponseDTO save(PhotoEditRequestDTO photoEditRequestDTO, UUID ownerId) {
        // Verify photo exists
        PhotoEntity photoEntity = photoRepository.findById(photoEditRequestDTO.photoId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoEditRequestDTO.photoId()));

        PhotoEditEntity photoEditToBeAdded = photoEditMapper.convertRequestDtoToEntity(photoEditRequestDTO);
        photoEditToBeAdded.setOwnerId(ownerId);
        photoEditToBeAdded.setEditedAt(ZonedDateTime.now());

        PhotoEditEntity photoEditAdded = photoEditRepository.save(photoEditToBeAdded);

        // Update the photo's edited status
        photoEntity.setIsEdited(true);
        photoRepository.save(photoEntity);

        return photoEditMapper.convertEntityToResponseDto(photoEditAdded);
    }

    @Override
    @Transactional
    public PhotoEditResponseDTO update(UUID id, PhotoEditRequestDTO photoEditRequestDTO, UUID ownerId) {
        PhotoEditEntity existingPhotoEdit = photoEditRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id));

        // Verify ownership
        if (!existingPhotoEdit.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id);
        }

        // Verify photo exists if being changed
        if (!existingPhotoEdit.getPhotoId().equals(photoEditRequestDTO.photoId())) {
            photoRepository.findById(photoEditRequestDTO.photoId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoEditRequestDTO.photoId()));
        }

        photoEditMapper.updatePhotoEditEntity(existingPhotoEdit, photoEditRequestDTO);
        PhotoEditEntity photoEditEntitySaved = photoEditRepository.save(existingPhotoEdit);

        return photoEditMapper.convertEntityToResponseDto(photoEditEntitySaved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PhotoEditEntity photoEditEntity = photoEditRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id));

        photoEditRepository.deleteById(photoEditEntity.getEditId());
    }
}