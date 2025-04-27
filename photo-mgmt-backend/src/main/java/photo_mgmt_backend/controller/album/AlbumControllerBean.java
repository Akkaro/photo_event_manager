package photo_mgmt_backend.controller.album;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.dto.user.UserResponseDTO;
import photo_mgmt_backend.service.album.AlbumService;
import photo_mgmt_backend.service.user.UserService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AlbumControllerBean implements AlbumController {

    private final AlbumService albumService;
    private final UserService userService;
    @Override
    public CollectionResponseDTO<AlbumResponseDTO> findAll(AlbumFilterDTO albumFilterDTO) {
        log.info("[ALBUM] Finding all albums: {}", albumFilterDTO);

        return albumService.findAll(albumFilterDTO);
    }

    @Override
    public AlbumResponseDTO findById(UUID id) {
        log.info("[ALBUM] Finding album by id: {}", id);

        return albumService.findById(id);
    }

    @Override
    public AlbumResponseDTO save(AlbumRequestDTO albumRequestDTO) {
        log.info("[ALBUM] Saving album: {}", albumRequestDTO);

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

        return albumService.save(albumRequestDTO, ownerId);
    }

    @Override
    public AlbumResponseDTO update(UUID id, AlbumRequestDTO albumRequestDTO) {
        log.info("[ALBUM] Updating album: {}", id);

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

        return albumService.update(id, albumRequestDTO, ownerId);
    }

    @Override
    public void delete(UUID id) {
        log.info("[ALBUM] Deleting album: {}", id);

        albumService.delete(id);
    }
}