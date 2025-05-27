package photo_mgmt_backend.controller.photo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditRequestDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditResponseDTO;
import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.service.photo.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import photo_mgmt_backend.service.photo_edit.PhotoEditService;
import photo_mgmt_backend.service.user.UserService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PhotoControllerBean implements PhotoController {

    private final PhotoService photoService;
    private final UserService userService;
    private final PhotoEditService photoEditService;

    @Override
    public CollectionResponseDTO<PhotoResponseDTO> findAll(PhotoFilterDTO photoFilterDTO) {
        log.info("[PHOTO] Finding all photos: {}", photoFilterDTO);
        return photoService.findAll(photoFilterDTO);
    }

    @Override
    public PhotoResponseDTO findById(UUID id) {
        log.info("[PHOTO] Finding photo by id: {}", id);
        return photoService.findById(id);
    }

    @Override
    public PhotoResponseDTO save(String photoJson, MultipartFile file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PhotoRequestDTO photoRequestDTO = objectMapper.readValue(photoJson, PhotoRequestDTO.class);

            log.info("[PHOTO] Saving photo: {}", photoRequestDTO);
            log.info("[PHOTO] File details - Name: {}, Size: {} bytes, Content-Type: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            UUID ownerId = getCurrentUserId();
            return photoService.save(photoRequestDTO, ownerId, file);
        } catch (Exception e) {
            log.error("[PHOTO] Error saving photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process photo upload", e);
        }
    }

    @Override
    public PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO) {
        log.info("[PHOTO] Updating photo: {}", id);
        UUID ownerId = getCurrentUserId();
        return photoService.update(id, photoRequestDTO, ownerId);
    }

    @Override
    public void delete(UUID id) {
        log.info("[PHOTO] Deleting photo: {}", id);
        photoService.delete(id);
    }

    @Override
    public PhotoEditResponseDTO editPhoto(UUID photoId, PhotoEditRequestDTO editRequest) {
        log.info("[PHOTO] Editing photo with advanced options: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequestWithPhotoId = new PhotoEditRequestDTO(
                photoId,
                editRequest.brightness(),
                editRequest.contrast(),
                editRequest.gamma(),
                editRequest.histogramEqualization(),
                editRequest.blurKernelSize(),
                editRequest.blurSigma(),
                editRequest.edgeDetectionType(),
                editRequest.morphologicalOperation(),
                editRequest.morphologicalKernelSize(),
                editRequest.morphologicalIterations(),
                editRequest.noiseReduction(),
                editRequest.thresholdValue(),
                editRequest.thresholdType(),
                editRequest.autoThreshold(),
                editRequest.hsvConversion(),
                editRequest.combinedProcessing()
        );

        return photoEditService.save(editRequestWithPhotoId, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editBrightnessContrast(UUID photoId, Double brightness, Double contrast) {
        log.info("[PHOTO] Editing photo brightness/contrast: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                BigDecimal.valueOf(brightness),
                BigDecimal.valueOf(contrast),
                null, null, null, null, null, null, null, null, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editGamma(UUID photoId, Double gamma) {
        log.info("[PHOTO] Editing photo gamma: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, BigDecimal.valueOf(gamma), null, null, null, null, null, null, null, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editHistogramEqualization(UUID photoId) {
        log.info("[PHOTO] Editing photo histogram equalization: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, true, null, null, null, null, null, null, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editBlur(UUID photoId, Integer kernelSize, Double sigma) {
        log.info("[PHOTO] Editing photo blur: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, kernelSize, BigDecimal.valueOf(sigma), null, null, null, null, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editEdgeDetection(UUID photoId, String type) {
        log.info("[PHOTO] Editing photo edge detection: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, type, null, null, null, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editMorphological(UUID photoId, String operation, Integer kernelSize, Integer iterations) {
        log.info("[PHOTO] Editing photo morphological: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, null, operation, kernelSize, iterations, null, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editDenoise(UUID photoId, String type) {
        log.info("[PHOTO] Editing photo denoise: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, null, null, null, null, type, null, null, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editThreshold(UUID photoId, Integer threshold, String type) {
        log.info("[PHOTO] Editing photo threshold: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, null, null, null, null, null, threshold, type, null, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editAutoThreshold(UUID photoId) {
        log.info("[PHOTO] Editing photo auto threshold: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, null, null, null, null, null, null, null, true, null, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    @Override
    public PhotoEditResponseDTO editHsvConvert(UUID photoId) {
        log.info("[PHOTO] Editing photo HSV convert: {}", photoId);
        UUID ownerId = getCurrentUserId();

        PhotoEditRequestDTO editRequest = new PhotoEditRequestDTO(
                photoId,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, true, false
        );

        return photoEditService.save(editRequest, ownerId);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserFilterDTO filter = new UserFilterDTO(
                null, email, null, null, 0, 1
        );

        CollectionResponseDTO<UserResponseDTO> users = userService.findAll(filter);

        if (users.elements().isEmpty()) {
            throw new UsernameNotFoundException("Current authenticated user not found in database");
        }

        return users.elements().get(0).userId();
    }
}