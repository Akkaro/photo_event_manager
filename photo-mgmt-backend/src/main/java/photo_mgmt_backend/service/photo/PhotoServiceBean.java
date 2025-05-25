package photo_mgmt_backend.service.photo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.entity.AlbumShareEntity;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.model.mapper.PhotoMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.album_share.AlbumShareRepository;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.photo.PhotoSpec;
import photo_mgmt_backend.repository.user.UserRepository;
import photo_mgmt_backend.service.cloudinary.CloudinaryService;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceBean implements PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final AlbumShareRepository albumShareRepository;
    private final UserRepository userRepository;
    private final PhotoSpec photoSpec;
    private final PhotoMapper photoMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public CollectionResponseDTO<PhotoResponseDTO> findAll(PhotoFilterDTO filter) {
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Check if user is ADMIN or MODERATOR - they can see all photos
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        log.info("[PHOTO] Finding photos, user: {}, isAdminOrModerator: {}, filter: {}", email, isAdminOrModerator, filter);

        // For regular users who filter by albumId
        if (!isAdminOrModerator && filter.albumId() != null) {
            // Get the album to check ownership
            AlbumEntity album = albumRepository.findById(filter.albumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, filter.albumId()));

            // Find the current user by email
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            // Check if user owns the album OR if the album is shared with the user
            boolean isOwner = album.getOwnerId().equals(currentUser.getUserId());
            boolean isSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                    filter.albumId(), currentUser.getUserId());

            if (!isOwner && !isSharedWithUser) {
                log.warn("[PHOTO] User {} is not the owner of album {} and album is not shared with them",
                        email, filter.albumId());
                // Return empty results if user doesn't own the album and it's not shared with them
                return createEmptyResponse(filter);
            }

            // User owns the album or it's shared with them, proceed with filter
            log.info("[PHOTO] User {} has access to album {} (owner: {}, shared: {})",
                    email, filter.albumId(), isOwner, isSharedWithUser);
        }

        // For regular users without albumId filter, we need to filter by their albums only
        if (!isAdminOrModerator && filter.albumId() == null) {
            // Find the current user by email
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            // Get all albums owned by the user
            List<AlbumEntity> userOwnedAlbums = albumRepository.findByOwnerId(currentUser.getUserId());

            // Get all albums shared with the user
            List<AlbumShareEntity> sharedAlbums = albumShareRepository.findBySharedWithUserId(currentUser.getUserId());
            List<UUID> sharedAlbumIds = sharedAlbums.stream()
                    .map(AlbumShareEntity::getAlbumId)
                    .toList();

            if (userOwnedAlbums.isEmpty() && sharedAlbumIds.isEmpty()) {
                log.info("[PHOTO] User {} has no albums (owned or shared), returning empty photo list", email);
                return createEmptyResponse(filter);
            }

            // Get all albums the user has access to (owned + shared)
            Set<UUID> accessibleAlbumIds = new HashSet<>();
            accessibleAlbumIds.addAll(userOwnedAlbums.stream().map(AlbumEntity::getAlbumId).collect(Collectors.toSet()));
            accessibleAlbumIds.addAll(sharedAlbumIds);

            // Get all photos from accessible albums
            List<PhotoEntity> userPhotos = photoRepository.findAll().stream()
                    .filter(photo -> accessibleAlbumIds.contains(photo.getAlbumId()))
                    .collect(Collectors.toList());

            // Manual pagination
            int startIndex = filter.pageNumber() * filter.pageSize();
            int endIndex = Math.min(startIndex + filter.pageSize(), userPhotos.size());

            List<PhotoEntity> pagedPhotos =
                    startIndex < userPhotos.size() ?
                            userPhotos.subList(startIndex, endIndex) :
                            List.of();

            log.info("[PHOTO] Found {} photos for user {} (from {} accessible albums)",
                    userPhotos.size(), email, accessibleAlbumIds.size());

            // Convert to response DTOs
            List<PhotoResponseDTO> photoDtos = photoMapper.convertEntitiesToResponseDtos(pagedPhotos);

            // Build and return the response
            return CollectionResponseDTO.<PhotoResponseDTO>builder()
                    .pageNumber(filter.pageNumber())
                    .pageSize(filter.pageSize())
                    .totalPages((int) Math.ceil((double) userPhotos.size() / filter.pageSize()))
                    .totalElements(userPhotos.size())
                    .elements(photoDtos)
                    .build();
        }

        // For ADMIN and MODERATOR, use normal filtering
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

    private CollectionResponseDTO<PhotoResponseDTO> createEmptyResponse(PhotoFilterDTO filter) {
        return CollectionResponseDTO.<PhotoResponseDTO>builder()
                .pageNumber(filter.pageNumber())
                .pageSize(filter.pageSize())
                .totalPages(0)
                .totalElements(0)
                .elements(List.of())
                .build();
    }

    @Override
    public PhotoResponseDTO findById(UUID id) {
        PhotoEntity photoEntity = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        // Check if user has permission to view this photo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            // Find the album to check ownership
            AlbumEntity album = albumRepository.findById(photoEntity.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoEntity.getAlbumId()));

            // Find the current user by email
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            // Check if user owns the album containing this photo
            if (!album.getOwnerId().equals(currentUser.getUserId())) {
                log.warn("[PHOTO] User {} attempted to access photo {} but is not the owner of album {}",
                        email, id, photoEntity.getAlbumId());
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }
        }

        return photoMapper.convertEntityToResponseDto(photoEntity);
    }

    @Override
    @Transactional
    public PhotoResponseDTO save(PhotoRequestDTO photoRequestDTO, UUID ownerId, MultipartFile file) {
        // Verify album exists
        AlbumEntity albumEntity = albumRepository.findById(photoRequestDTO.albumId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));

        // Verify user has permission (admin/moderator, owner of the album, OR album is shared with user)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            // For regular users, check if they own the album OR if it's shared with them
            boolean isOwner = albumEntity.getOwnerId().equals(ownerId);
            boolean isSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                    photoRequestDTO.albumId(), ownerId);

            if (!isOwner && !isSharedWithUser) {
                log.warn("[PHOTO] User {} tried to upload to album {} but doesn't own it and it's not shared with them",
                        ownerId, photoRequestDTO.albumId());
                throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId());
            }

            log.info("[PHOTO] User {} has permission to upload to album {} (owner: {}, shared: {})",
                    ownerId, photoRequestDTO.albumId(), isOwner, isSharedWithUser);
        }

        PhotoEntity photoToBeAdded = photoMapper.convertRequestDtoToEntity(photoRequestDTO);
        photoToBeAdded.setPhotoName(photoRequestDTO.photoName());
        photoToBeAdded.setOwnerId(ownerId);
        photoToBeAdded.setUploadedAt(ZonedDateTime.now());
        photoToBeAdded.setIsEdited(false);

        // Upload image to Cloudinary
        String path = cloudinaryService.uploadImage(file, ownerId);
        photoToBeAdded.setPath(path);

        PhotoEntity photoAdded = photoRepository.save(photoToBeAdded);
        log.info("[PHOTO] Added new photo with ID: {} to album: {}", photoAdded.getPhotoId(), photoRequestDTO.albumId());

        return photoMapper.convertEntityToResponseDto(photoAdded);
    }

    @Override
    @Transactional
    public PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO, UUID ownerId) {
        PhotoEntity existingPhoto = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        // Check if album is changing
        boolean albumChanging = !existingPhoto.getAlbumId().equals(photoRequestDTO.albumId());

        // Verify album exists if being changed
        if (albumChanging) {
            albumRepository.findById(photoRequestDTO.albumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));
        }

        // Verify user has permission
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // For non-admin users, check permissions
        if (!isAdmin) {
            // Check current album ownership
            AlbumEntity currentAlbum = albumRepository.findById(existingPhoto.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, existingPhoto.getAlbumId()));

            if (!currentAlbum.getOwnerId().equals(ownerId)) {
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }

            // If album is changing, check new album ownership
            if (albumChanging) {
                AlbumEntity newAlbum = albumRepository.findById(photoRequestDTO.albumId())
                        .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));

                if (!newAlbum.getOwnerId().equals(ownerId)) {
                    throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId());
                }
            }
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

        // Verify user has permission
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // For non-admin users, check permissions
        if (!isAdmin) {
            // Get the album to check ownership
            AlbumEntity album = albumRepository.findById(photoEntity.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoEntity.getAlbumId()));

            // Find the current user by email
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            // Check if user owns the album containing this photo
            if (!album.getOwnerId().equals(currentUser.getUserId())) {
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }
        }

        // Delete the image from Cloudinary
        String publicId = cloudinaryService.extractPublicIdFromUrl(photoEntity.getPath());
        if (publicId != null) {
            cloudinaryService.deleteImage(publicId);
        }

        photoRepository.deleteById(photoEntity.getPhotoId());
        log.info("[PHOTO] Deleted photo with ID: {}", id);
    }
}