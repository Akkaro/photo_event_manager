package photo_mgmt_backend.service.photo_edit;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import photo_mgmt_backend.service.cloudinary.CloudinaryService;
import photo_mgmt_backend.service.photo_version.PhotoVersionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoEditServiceBean implements PhotoEditService {

    private final PhotoEditRepository photoEditRepository;
    private final PhotoRepository photoRepository;
    private final PhotoEditSpec photoEditSpec;
    private final PhotoEditMapper photoEditMapper;
    private final CloudinaryService cloudinaryService;
    private final PhotoVersionService photoVersionService;

    @Value("${photo.editing.tools.path:C:/photo-editing-tools}")
    private String editingToolsPath;

    @Value("${photo.editing.temp.path:C:/temp/photo-editing}")
    private String tempPath;

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
        PhotoEntity photoEntity = photoRepository.findById(photoEditRequestDTO.photoId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoEditRequestDTO.photoId()));

        try {
            String currentImageUrl = photoEntity.getPath();

            if (photoEntity.getOriginalPath() == null || photoEntity.getOriginalPath().isEmpty()) {
                photoEntity.setOriginalPath(currentImageUrl);
                log.info("[PHOTO_EDIT] Setting original path for photo {} to {}", photoEditRequestDTO.photoId(), currentImageUrl);
            }

            String currentImagePath = downloadImageFromCloudinary(currentImageUrl);

            String editedImagePath = applyPhotoEdits(currentImagePath, photoEditRequestDTO);

            String editedImageUrl = uploadEditedImageToCloudinary(editedImagePath, ownerId);

            Integer nextVersion = photoVersionService.getNextVersionNumber(photoEditRequestDTO.photoId());

            PhotoEditEntity photoEditToBeAdded = photoEditMapper.convertRequestDtoToEntity(photoEditRequestDTO);
            photoEditToBeAdded.setOwnerId(ownerId);
            photoEditToBeAdded.setEditedAt(ZonedDateTime.now());

            photoEditToBeAdded.setVersionNumber(nextVersion);
            photoEditToBeAdded.setPreviousVersionUrl(currentImageUrl);
            photoEditToBeAdded.setResultVersionUrl(editedImageUrl);

            PhotoEditEntity photoEditAdded = photoEditRepository.save(photoEditToBeAdded);

            photoEntity.setIsEdited(true);
            photoEntity.setPath(editedImageUrl);
            photoRepository.save(photoEntity);

            cleanupTempFiles(currentImagePath, editedImagePath);

            log.info("[PHOTO_EDIT] Created version {} for photo {}, original path preserved: {}",
                    nextVersion, photoEditRequestDTO.photoId(), photoEntity.getOriginalPath());

            return photoEditMapper.convertEntityToResponseDto(photoEditAdded);

        } catch (Exception e) {
            log.error("Error applying photo edits: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to apply photo edits", e);
        }
    }

    private String downloadImageFromCloudinary(String imageUrl) throws IOException {
        Path tempDir = Paths.get(tempPath);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        String filename = UUID.randomUUID().toString() + "_original.jpg";
        Path imagePath = tempDir.resolve(filename);

        ProcessBuilder pb = new ProcessBuilder("curl", "-o", imagePath.toString(), imageUrl);
        Process process = pb.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to download image from Cloudinary");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }

        return imagePath.toString();
    }

    private String applyPhotoEdits(String originalImagePath, PhotoEditRequestDTO editRequest) throws IOException {
        String editedImagePath = originalImagePath.replace("_original.jpg", "_edited.jpg");

        List<String> command = new ArrayList<>();
        command.add(Paths.get(editingToolsPath, "OpenCVApplication.exe").toString());
        command.add(originalImagePath);
        command.add(editedImagePath);

        if (Boolean.TRUE.equals(editRequest.combinedProcessing())) {
            buildCombinedCommand(command, editRequest);
        } else {
            buildSingleOperationCommand(command, editRequest);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        executeProcess(pb, "photo editing");
        return editedImagePath;
    }

    private void buildCombinedCommand(List<String> command, PhotoEditRequestDTO editRequest) {
        command.add("combined");

        command.add(String.valueOf(editRequest.brightness() != null ? editRequest.brightness().doubleValue() : 0));

        command.add(String.valueOf(editRequest.contrast() != null ? editRequest.contrast().doubleValue() : 1.0));

        command.add(String.valueOf(editRequest.gamma() != null ? editRequest.gamma().doubleValue() : 1.0));

        command.add(Boolean.TRUE.equals(editRequest.histogramEqualization()) ? "1" : "0");

        command.add(editRequest.noiseReduction() != null ? editRequest.noiseReduction() : "none");

        command.add(editRequest.edgeDetectionType() != null ? editRequest.edgeDetectionType() : "none");

        command.add(String.valueOf(editRequest.thresholdValue() != null ? editRequest.thresholdValue() : -1));
    }

    private void buildSingleOperationCommand(List<String> command, PhotoEditRequestDTO editRequest) {
        if (editRequest.brightness() != null || editRequest.contrast() != null) {
            command.add("brightness_contrast");
            command.add(String.valueOf(editRequest.brightness() != null ? editRequest.brightness().doubleValue() : 0));
            command.add(String.valueOf(editRequest.contrast() != null ? editRequest.contrast().doubleValue() : 1.0));
        } else if (editRequest.gamma() != null) {
            command.add("gamma");
            command.add(String.valueOf(editRequest.gamma().doubleValue()));
        } else if (Boolean.TRUE.equals(editRequest.histogramEqualization())) {
            command.add("histogram_eq");
        } else if (editRequest.blurKernelSize() != null) {
            command.add("blur");
            command.add(String.valueOf(editRequest.blurKernelSize()));
            command.add(String.valueOf(editRequest.blurSigma() != null ? editRequest.blurSigma().doubleValue() : 2.0));
        } else if (editRequest.edgeDetectionType() != null) {
            command.add("edge_detection");
            command.add(editRequest.edgeDetectionType());
        } else if (editRequest.morphologicalOperation() != null) {
            command.add("morphological");
            command.add(editRequest.morphologicalOperation());
            command.add(String.valueOf(editRequest.morphologicalKernelSize() != null ? editRequest.morphologicalKernelSize() : 5));
            command.add(String.valueOf(editRequest.morphologicalIterations() != null ? editRequest.morphologicalIterations() : 1));
        } else if (editRequest.noiseReduction() != null) {
            command.add("denoise");
            command.add(editRequest.noiseReduction());
        } else if (editRequest.thresholdValue() != null) {
            command.add("threshold");
            command.add(String.valueOf(editRequest.thresholdValue()));
            command.add(editRequest.thresholdType() != null ? editRequest.thresholdType() : "binary");
        } else if (Boolean.TRUE.equals(editRequest.autoThreshold())) {
            command.add("auto_threshold");
        } else if (Boolean.TRUE.equals(editRequest.hsvConversion())) {
            command.add("hsv_convert");
        } else {
            command.add("brightness_contrast");
            command.add("0");
            command.add("1.0");
        }
    }

    private void executeProcess(ProcessBuilder pb, String operation) throws IOException {
        log.info("Executing {} with command: {}", operation, String.join(" ", pb.command()));

        Process process = pb.start();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Process output: {}", line);
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                log.error("Process error: {}", line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to execute " + operation + ". Exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Process interrupted during " + operation, e);
        }
    }

    private String uploadEditedImageToCloudinary(String editedImagePath, UUID ownerId) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(editedImagePath));
            return cloudinaryService.uploadEditedImage(imageBytes, ownerId);
        } catch (IOException e) {
            log.error("Failed to upload edited image to Cloudinary", e);
            throw new RuntimeException("Failed to upload edited image", e);
        }
    }

    private void cleanupTempFiles(String... filePaths) {
        for (String filePath : filePaths) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
                log.debug("Cleaned up temporary file: {}", filePath);
            } catch (IOException e) {
                log.warn("Failed to cleanup temporary file: {}", filePath, e);
            }
        }
    }

    @Override
    @Transactional
    public PhotoEditResponseDTO update(UUID id, PhotoEditRequestDTO photoEditRequestDTO, UUID ownerId) {
        PhotoEditEntity existingPhotoEdit = photoEditRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id));

        if (!existingPhotoEdit.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.PHOTO_EDIT_NOT_FOUND, id);
        }

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