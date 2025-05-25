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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
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
        // Verify photo exists
        PhotoEntity photoEntity = photoRepository.findById(photoEditRequestDTO.photoId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, photoEditRequestDTO.photoId()));

        try {
            // Download original image from Cloudinary
            String originalImagePath = downloadImageFromCloudinary(photoEntity.getPath());

            // Apply edits using C++ tools
            String editedImagePath = applyPhotoEdits(originalImagePath, photoEditRequestDTO);

            // Upload edited image back to Cloudinary
            String editedImageUrl = uploadEditedImageToCloudinary(editedImagePath, ownerId);

            // Save edit record
            PhotoEditEntity photoEditToBeAdded = photoEditMapper.convertRequestDtoToEntity(photoEditRequestDTO);
            photoEditToBeAdded.setOwnerId(ownerId);
            photoEditToBeAdded.setEditedAt(ZonedDateTime.now());

            PhotoEditEntity photoEditAdded = photoEditRepository.save(photoEditToBeAdded);

            // Update the photo's edited status and path
            photoEntity.setIsEdited(true);
            photoEntity.setPath(editedImageUrl);
            photoRepository.save(photoEntity);

            // Clean up temporary files
            cleanupTempFiles(originalImagePath, editedImagePath);

            return photoEditMapper.convertEntityToResponseDto(photoEditAdded);

        } catch (Exception e) {
            log.error("Error applying photo edits: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to apply photo edits", e);
        }
    }

    private String downloadImageFromCloudinary(String imageUrl) throws IOException {
        // Create temp directory if it doesn't exist
        Path tempDir = Paths.get(tempPath);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_original.jpg";
        Path imagePath = tempDir.resolve(filename);

        // Download image using curl or similar
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

        ProcessBuilder pb = new ProcessBuilder(
                Paths.get(editingToolsPath, "photo_editor.exe").toString(),  // or "photo_editor.exe"
                originalImagePath,
                editedImagePath,
                "combined",
                String.valueOf(editRequest.brightness() != null ? editRequest.brightness().intValue() : 0),
                String.valueOf(editRequest.contrast() != null ? editRequest.contrast().doubleValue() : 1.0)
        );

        executeProcess(pb, "photo editing");
        return editedImagePath;
    }

    private void executeProcess(ProcessBuilder pb, String operation) throws IOException {
        log.info("Executing {} with command: {}", operation, String.join(" ", pb.command()));

        Process process = pb.start();

        try {
            // Read process output for debugging
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Process output: {}", line);
            }

            // Read error output
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