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
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.photo_edit.PhotoEditRepository;
import photo_mgmt_backend.repository.user.UserRepository;
import photo_mgmt_backend.service.cloudinary.CloudinaryService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoVersionServiceBean implements PhotoVersionService {

    private final PhotoRepository photoRepository;
    private final PhotoEditRepository photoEditRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public PhotoVersionHistoryDTO getVersionHistory(UUID photoId, UUID requestingUserId) {
        PhotoEntity photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoId));

        List<PhotoEditEntity> editEntities = photoEditRepository.findByPhotoIdOrderByVersionNumberDesc(photoId);

        List<PhotoVersionDTO> versions = new ArrayList<>();

        String ownerName = "Unknown";
        if (photo.getOwner() != null) {
            ownerName = photo.getOwner().getUserName();
        }

        boolean originalIsCurrent = editEntities.isEmpty() ||
                photo.getPath().equals(photo.getOriginalPath());

        versions.add(new PhotoVersionDTO(
                null,
                0,
                photo.getOriginalPath(),
                "Original (unedited)",
                photo.getUploadedAt(),
                ownerName,
                originalIsCurrent
        ));

        Integer currentVersion = getCurrentVersionNumber(photo, editEntities);
        for (PhotoEditEntity editEntity : editEntities) {
            if (isRevertOperation(editEntity)) {
                continue;
            }

            String editOwnerName = editEntity.getOwner() != null ?
                    editEntity.getOwner().getUserName() : "Unknown";

            versions.add(new PhotoVersionDTO(
                    editEntity.getEditId(),
                    editEntity.getVersionNumber(),
                    editEntity.getResultVersionUrl() != null ?
                            editEntity.getResultVersionUrl() : photo.getPath(),
                    generateEditDescription(editEntity),
                    editEntity.getEditedAt(),
                    editOwnerName,
                    editEntity.getVersionNumber().equals(currentVersion) &&
                            !originalIsCurrent
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

        return photo.getOriginalPath();
    }

    @Override
    @Transactional
    public PhotoVersionDTO revertToVersion(RevertToVersionRequestDTO request, UUID requestingUserId) {
        PhotoEntity photo = photoRepository.findById(request.photoId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, request.photoId()));

        String targetImageUrl;
        String description;
        boolean isRevertingToOriginal = request.targetVersion() == 0;

        if (isRevertingToOriginal) {
            targetImageUrl = photo.getOriginalPath();
            description = "Reverted to original";

            photo.setPath(targetImageUrl);
            photo.setIsEdited(false);
        } else {
            PhotoEditEntity targetVersion = photoEditRepository.findByPhotoIdAndVersionNumber(
                            request.photoId(), request.targetVersion())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND,
                            "Version " + request.targetVersion()));

            targetImageUrl = targetVersion.getResultVersionUrl() != null ?
                    targetVersion.getResultVersionUrl() : photo.getPath();
            description = "Reverted to version " + request.targetVersion();

            photo.setPath(targetImageUrl);
            photo.setIsEdited(true);
        }

        photoRepository.save(photo);

        PhotoEditEntity revertRecord = createRevertRecord(photo, request, requestingUserId, targetImageUrl, description);
        PhotoEditEntity savedRevert = photoEditRepository.save(revertRecord);

        log.info("[PHOTO_VERSION] Reverted photo {} to version {}", request.photoId(), request.targetVersion());

        String userName = getUserName(requestingUserId);

        return new PhotoVersionDTO(
                savedRevert.getEditId(),
                request.targetVersion(),
                targetImageUrl,
                description,
                savedRevert.getEditedAt(),
                userName,
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
        if (isRevertOperation(editEntity)) {
            return "Revert operation";
        }

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
            return "Edit operation";
        }

        return String.join(", ", operations);
    }

    private Integer getCurrentVersionNumber(PhotoEntity photo, List<PhotoEditEntity> editEntities) {
        if (editEntities.isEmpty()) {
            return 0;
        }

        if (photo.getPath().equals(photo.getOriginalPath())) {
            return 0;
        }

        for (PhotoEditEntity edit : editEntities) {
            if (edit.getResultVersionUrl() != null &&
                    edit.getResultVersionUrl().equals(photo.getPath())) {
                return edit.getVersionNumber();
            }
        }

        return editEntities.get(0).getVersionNumber();
    }

    private boolean isRevertOperation(PhotoEditEntity editEntity) {
        return editEntity.getBrightness() == null &&
                editEntity.getContrast() == null &&
                editEntity.getGamma() == null &&
                !Boolean.TRUE.equals(editEntity.getHistogramEqualization()) &&
                editEntity.getBlurKernelSize() == null &&
                editEntity.getEdgeDetectionType() == null &&
                editEntity.getMorphologicalOperation() == null &&
                editEntity.getNoiseReduction() == null &&
                editEntity.getThresholdValue() == null &&
                !Boolean.TRUE.equals(editEntity.getAutoThreshold()) &&
                !Boolean.TRUE.equals(editEntity.getHsvConversion()) &&
                !Boolean.TRUE.equals(editEntity.getCombinedProcessing());
    }

    private PhotoEditEntity createRevertRecord(PhotoEntity photo, RevertToVersionRequestDTO request,
                                               UUID requestingUserId, String targetImageUrl, String description) {
        PhotoEditEntity revertRecord = new PhotoEditEntity();
        revertRecord.setPhotoId(request.photoId());
        revertRecord.setOwnerId(requestingUserId);
        revertRecord.setVersionNumber(-1);
        revertRecord.setPreviousVersionUrl(photo.getPath());
        revertRecord.setResultVersionUrl(targetImageUrl);
        revertRecord.setEditedAt(ZonedDateTime.now());

        return revertRecord;
    }

    private String getUserName(UUID userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getUserName)
                .orElse("Unknown User");
    }
}