package photo_mgmt_backend.service.photo_version;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.photo_version.PhotoVersionDTO;
import photo_mgmt_backend.model.dto.photo_version.PhotoVersionHistoryDTO;
import photo_mgmt_backend.model.dto.photo_version.RevertToVersionRequestDTO;
import photo_mgmt_backend.model.entity.PhotoEditEntity;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.photo_edit.PhotoEditRepository;
import photo_mgmt_backend.service.cloudinary.CloudinaryService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoVersionServiceBean implements PhotoVersionService {

    private final PhotoRepository photoRepository;
    private final PhotoEditRepository photoEditRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public PhotoVersionHistoryDTO getVersionHistory(UUID photoId, UUID requestingUserId) {
        // Verify photo exists and user has access
        PhotoEntity photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoId));

        // TODO: Add permission check here (owner or shared album access)

        // Get all versions ordered by version number (newest first)
        List<PhotoEditEntity> editEntities = photoEditRepository.findByPhotoIdOrderByVersionNumberDesc(photoId);

        // Convert to DTOs
        List<PhotoVersionDTO> versions = new ArrayList<>();

        // Add original version (version 0)
        versions.add(new PhotoVersionDTO(
                null, // No edit ID for original
                0,
                photo.getOriginalPath(),
                "Original (unedited)",
                photo.getUploadedAt(),
                photo.getOwner() != null ? photo.getOwner().getUserName() : "Unknown",
                editEntities.isEmpty() // Is current if no edits exist
        ));

        // Add edit versions
        Integer currentVersion = getCurrentVersionNumber(editEntities);
        for (PhotoEditEntity editEntity : editEntities) {
            versions.add(new PhotoVersionDTO(
                    editEntity.getEditId(),
                    editEntity.getVersionNumber(),
                    editEntity.getResultVersionUrl() != null ? editEntity.getResultVersionUrl() : photo.getPath(),
                    generateEditDescription(editEntity),
                    editEntity.getEditedAt(),
                    editEntity.getOwner() != null ? editEntity.getOwner().getUserName() : "Unknown",
                    editEntity.getVersionNumber().equals(currentVersion)
            ));
        }

        return new PhotoVersionHistoryDTO(
                photoId,
                photo.getPhotoName(),
                photo.getOriginalPath(),
                photo.getPath(),
                currentVersion,
                versions.size(),
                versions
        );
    }

    @Override
    public String getOriginalImageUrl(UUID photoId, UUID requestingUserId) {
        PhotoEntity photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoId));

        // TODO: Add permission check here

        return photo.getOriginalPath();
    }

    @Override
    @Transactional
    public PhotoVersionDTO revertToVersion(RevertToVersionRequestDTO request, UUID requestingUserId) {
        PhotoEntity photo = photoRepository.findById(request.photoId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, request.photoId()));

        // TODO: Add permission check here

        String targetImageUrl;
        String description;

        if (request.targetVersion() == 0) {
            // Reverting to original
            targetImageUrl = photo.getOriginalPath();
            description = "Reverted to original";
        } else {
            // Reverting to specific edit version
            PhotoEditEntity targetVersion = photoEditRepository.findByPhotoIdAndVersionNumber(
                            request.photoId(), request.targetVersion())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND,
                            "Version " + request.targetVersion()));

            targetImageUrl = targetVersion.getResultVersionUrl() != null ?
                    targetVersion.getResultVersionUrl() : photo.getPath();
            description = "Reverted to version " + request.targetVersion();
        }

        // Update photo's current path
        photo.setPath(targetImageUrl);
        photo.setIsEdited(request.targetVersion() > 0);
        photoRepository.save(photo);

        // Create a new edit entry for the revert action
        Integer nextVersion = getNextVersionNumber(request.photoId());
        PhotoEditEntity revertEdit = new PhotoEditEntity();
        revertEdit.setPhotoId(request.photoId());
        revertEdit.setOwnerId(requestingUserId);
        revertEdit.setVersionNumber(nextVersion);
        revertEdit.setPreviousVersionUrl(photo.getPath());
        revertEdit.setResultVersionUrl(targetImageUrl);
        revertEdit.setEditedAt(ZonedDateTime.now());
        // Note: All other edit parameters will be null, indicating this is a revert operation

        PhotoEditEntity savedRevert = photoEditRepository.save(revertEdit);

        log.info("[PHOTO_VERSION] Reverted photo {} to version {}", request.photoId(), request.targetVersion());

        return new PhotoVersionDTO(
                savedRevert.getEditId(),
                nextVersion,
                targetImageUrl,
                description,
                savedRevert.getEditedAt(),
                "Current User", // TODO: Get actual username
                true
        );
    }

    @Override
    public Integer getNextVersionNumber(UUID photoId) {
        return photoEditRepository.findMaxVersionNumberByPhotoId(photoId)
                .map(max -> max + 1)
                .orElse(1);
    }

    @Override
    public String generateEditDescription(PhotoEditEntity editEntity) {
        List<String> operations = new ArrayList<>();

        if (editEntity.getBrightness() != null && editEntity.getBrightness().doubleValue() != 0) {
            operations.add("Brightness: " + editEntity.getBrightness());
        }
        if (editEntity.getContrast() != null && editEntity.getContrast().doubleValue() != 1.0) {
            operations.add("Contrast: " + editEntity.getContrast());
        }
        if (editEntity.getGamma() != null && editEntity.getGamma().doubleValue() != 1.0) {
            operations.add("Gamma: " + editEntity.getGamma());
        }
        if (Boolean.TRUE.equals(editEntity.getHistogramEqualization())) {
            operations.add("Histogram Equalization");
        }
        if (editEntity.getBlurKernelSize() != null) {
            operations.add("Blur (size: " + editEntity.getBlurKernelSize() + ")");
        }
        if (editEntity.getEdgeDetectionType() != null) {
            operations.add("Edge Detection (" + editEntity.getEdgeDetectionType() + ")");
        }
        if (editEntity.getMorphologicalOperation() != null) {
            operations.add("Morphological " + editEntity.getMorphologicalOperation());
        }
        if (editEntity.getNoiseReduction() != null) {
            operations.add("Noise Reduction (" + editEntity.getNoiseReduction() + ")");
        }
        if (editEntity.getThresholdValue() != null) {
            operations.add("Threshold: " + editEntity.getThresholdValue());
        }
        if (Boolean.TRUE.equals(editEntity.getAutoThreshold())) {
            operations.add("Auto Threshold");
        }
        if (Boolean.TRUE.equals(editEntity.getHsvConversion())) {
            operations.add("HSV Conversion");
        }
        if (Boolean.TRUE.equals(editEntity.getCombinedProcessing())) {
            operations.add("Combined Processing");
        }

        if (operations.isEmpty()) {
            return "Revert operation";
        }

        return String.join(", ", operations);
    }

    private Integer getCurrentVersionNumber(List<PhotoEditEntity> editEntities) {
        return editEntities.isEmpty() ? 0 : editEntities.get(0).getVersionNumber();
    }
}