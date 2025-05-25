package photo_mgmt_backend.service.album;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.DuplicateDataException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.model.mapper.AlbumMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.album.AlbumSpec;
import photo_mgmt_backend.repository.user.UserRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumServiceBean implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumSpec albumSpec;
    private final AlbumMapper albumMapper;
    private final UserRepository userRepository;

    @Override
    public CollectionResponseDTO<AlbumResponseDTO> findAll(AlbumFilterDTO filter) {
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is ADMIN or MODERATOR - they can see all albums
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        // For regular users, we need to filter by their own ID
        if (!isAdminOrModerator) {
            // Find the current user by email
            String email = authentication.getName();
            log.info("[ALBUM] Finding albums for user email: {}", email);

            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            log.info("[ALBUM] Found user with ID: {}", currentUser.getUserId());

            // For regular users, fetch only their albums directly from the repository
            List<AlbumEntity> userAlbums = albumRepository.findByOwnerId(currentUser.getUserId());

            // Manually create the pagination result
            int startIndex = filter.pageNumber() * filter.pageSize();
            int endIndex = Math.min(startIndex + filter.pageSize(), userAlbums.size());

            // Handle potential out of bounds
            List<AlbumEntity> pagedAlbums =
                    startIndex < userAlbums.size() ?
                            userAlbums.subList(startIndex, endIndex) :
                            List.of();

            log.info("[ALBUM] Found {} albums for user", userAlbums.size());

            // Convert to response DTOs
            List<AlbumResponseDTO> albumDtos = albumMapper.convertEntitiesToResponseDtos(pagedAlbums);

            // Build and return the response
            return CollectionResponseDTO.<AlbumResponseDTO>builder()
                    .pageNumber(filter.pageNumber())
                    .pageSize(filter.pageSize())
                    .totalPages((int) Math.ceil((double) userAlbums.size() / filter.pageSize()))
                    .totalElements(userAlbums.size())
                    .elements(albumDtos)
                    .build();
        }

        // For ADMIN and MODERATOR, use normal filtering
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

        // Check if user has permission to view this album
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            // For regular users, check if they own the album
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            if (!albumEntity.getOwnerId().equals(currentUser.getUserId())) {
                // If user doesn't own the album and isn't admin/moderator, throw not found
                throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id);
            }
        }

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
        log.info("[ALBUM] Created new album with ID: {} for owner: {}", albumAdded.getAlbumId(), ownerId);

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

        // Check permissions before deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // For non-admin users, check if they own the album
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            if (!albumEntity.getOwnerId().equals(currentUser.getUserId())) {
                // If user doesn't own the album and isn't admin, throw not found
                throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id);
            }
        }

        albumRepository.deleteById(albumEntity.getAlbumId());
    }

    // Helper method for QR code generation
    private String generateQrCode(AlbumEntity album) {
        // In a real implementation, this would generate an actual QR code
        // For now, just returning a placeholder string
        return "qr_code_" + album.getAlbumName();
    }
}