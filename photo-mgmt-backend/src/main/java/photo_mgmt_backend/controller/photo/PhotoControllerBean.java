package photo_mgmt_backend.controller.photo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.dto.user.UserRequestDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.security.service.user.UserDetailsServiceBean;
import photo_mgmt_backend.service.photo.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import photo_mgmt_backend.service.user.UserService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PhotoControllerBean implements PhotoController {

    private final PhotoService photoService;
    private final UserService userService;

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
            // Convert JSON string to PhotoRequestDTO
            ObjectMapper objectMapper = new ObjectMapper();
            PhotoRequestDTO photoRequestDTO = objectMapper.readValue(photoJson, PhotoRequestDTO.class);

            log.info("[PHOTO] Saving photo: {}", photoRequestDTO);
            log.info("[PHOTO] File details - Name: {}, Size: {} bytes, Content-Type: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            // Get the current authenticated user's username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Create a filter to find the user by username using the record constructor
            UserFilterDTO filter = new UserFilterDTO(
                    null,  // userName
                    email,            // email
                    null,            // role
                    null,           //createdAt
                    0,               // pageNumber
                    1                // pageSize - We only need one result
            );

            // Get user from the service
            CollectionResponseDTO<UserResponseDTO> users = userService.findAll(filter);

            // Assuming the username is unique, get the first user's ID
            if (users.elements().isEmpty()) {
                throw new UsernameNotFoundException("Current authenticated user not found in database");
            }

            UUID ownerId = users.elements().get(0).userId();

            // Pass the owner ID to the service
            return photoService.save(photoRequestDTO, ownerId, file);
        } catch (Exception e) {
            log.error("[PHOTO] Error saving photo: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process photo upload", e);
        }
    }

    @Override
    public PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO) {
        log.info("[PHOTO] Updating photo: {}", id);

        // Get the current authenticated user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Create a filter to find the user by username using the record constructor
        UserFilterDTO filter = new UserFilterDTO(
                currentUsername,  // userName
                null,            // email
                null,            // role
                null,           //createdAt
                0,               // pageNumber
                1                // pageSize - We only need one result
        );

        // Get user from the service
        CollectionResponseDTO<UserResponseDTO> users = userService.findAll(filter);

        // Assuming the username is unique, get the first user's ID
        if (users.elements().isEmpty()) {
            throw new UsernameNotFoundException("Current authenticated user not found in database");
        }

        UUID ownerId = users.elements().get(0).userId();

        return photoService.update(id, photoRequestDTO, ownerId);
    }

    @Override
    public void delete(UUID id) {
        log.info("[PHOTO] Deleting photo: {}", id);

        photoService.delete(id);
    }
}