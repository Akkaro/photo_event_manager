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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        log.info("[PHOTO] Finding photos, user: {}, isAdminOrModerator: {}, filter: {}", email, isAdminOrModerator, filter);

        if (!isAdminOrModerator && filter.albumId() != null) {
            AlbumEntity album = albumRepository.findById(filter.albumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, filter.albumId()));

            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            boolean isOwner = album.getOwnerId().equals(currentUser.getUserId());
            boolean isSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                    filter.albumId(), currentUser.getUserId());

            if (!isOwner && !isSharedWithUser) {
                log.warn("[PHOTO] User {} is not the owner of album {} and album is not shared with them",
                        email, filter.albumId());
                return createEmptyResponse(filter);
            }

            log.info("[PHOTO] User {} has access to album {} (owner: {}, shared: {})",
                    email, filter.albumId(), isOwner, isSharedWithUser);
        }

        if (!isAdminOrModerator && filter.albumId() == null) {
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            List<AlbumEntity> userOwnedAlbums = albumRepository.findByOwnerId(currentUser.getUserId());

            List<AlbumShareEntity> sharedAlbums = albumShareRepository.findBySharedWithUserId(currentUser.getUserId());
            List<UUID> sharedAlbumIds = sharedAlbums.stream()
                    .map(AlbumShareEntity::getAlbumId)
                    .toList();

            if (userOwnedAlbums.isEmpty() && sharedAlbumIds.isEmpty()) {
                log.info("[PHOTO] User {} has no albums (owned or shared), returning empty photo list", email);
                return createEmptyResponse(filter);
            }

            Set<UUID> accessibleAlbumIds = new HashSet<>();
            accessibleAlbumIds.addAll(userOwnedAlbums.stream().map(AlbumEntity::getAlbumId).collect(Collectors.toSet()));
            accessibleAlbumIds.addAll(sharedAlbumIds);

            List<PhotoEntity> userPhotos = photoRepository.findAll().stream()
                    .filter(photo -> accessibleAlbumIds.contains(photo.getAlbumId()))
                    .collect(Collectors.toList());

            int startIndex = filter.pageNumber() * filter.pageSize();
            int endIndex = Math.min(startIndex + filter.pageSize(), userPhotos.size());

            List<PhotoEntity> pagedPhotos =
                    startIndex < userPhotos.size() ?
                            userPhotos.subList(startIndex, endIndex) :
                            List.of();

            log.info("[PHOTO] Found {} photos for user {} (from {} accessible albums)",
                    userPhotos.size(), email, accessibleAlbumIds.size());

            List<PhotoResponseDTO> photoDtos = photoMapper.convertEntitiesToResponseDtos(pagedPhotos);

            return CollectionResponseDTO.<PhotoResponseDTO>builder()
                    .pageNumber(filter.pageNumber())
                    .pageSize(filter.pageSize())
                    .totalPages((int) Math.ceil((double) userPhotos.size() / filter.pageSize()))
                    .totalElements(userPhotos.size())
                    .elements(photoDtos)
                    .build();
        }

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            AlbumEntity album = albumRepository.findById(photoEntity.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoEntity.getAlbumId()));

            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            boolean isOwner = album.getOwnerId().equals(currentUser.getUserId());
            boolean isSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                    photoEntity.getAlbumId(), currentUser.getUserId());

            if (!isOwner && !isSharedWithUser) {
                log.warn("[PHOTO] User {} attempted to access photo {} but is not the owner of album {} and album is not shared with them",
                        email, id, photoEntity.getAlbumId());
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }

            log.info("[PHOTO] User {} has access to photo {} (owner: {}, shared: {})",
                    email, id, isOwner, isSharedWithUser);
        }

        return photoMapper.convertEntityToResponseDto(photoEntity);
    }

    @Override
    @Transactional
    public PhotoResponseDTO save(PhotoRequestDTO photoRequestDTO, UUID ownerId, MultipartFile file) {
        AlbumEntity albumEntity = albumRepository.findById(photoRequestDTO.albumId())
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
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

        String imagePath = cloudinaryService.uploadImage(file, ownerId);
        photoToBeAdded.setPath(imagePath);

        photoToBeAdded.setOriginalPath(imagePath);

        PhotoEntity photoAdded = photoRepository.save(photoToBeAdded);
        log.info("[PHOTO] Added new photo with ID: {} to album: {}, original path set to: {}",
                photoAdded.getPhotoId(), photoRequestDTO.albumId(), imagePath);

        return photoMapper.convertEntityToResponseDto(photoAdded);
    }

    @Override
    @Transactional
    public PhotoResponseDTO update(UUID id, PhotoRequestDTO photoRequestDTO, UUID ownerId) {
        PhotoEntity existingPhoto = photoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id));

        boolean albumChanging = !existingPhoto.getAlbumId().equals(photoRequestDTO.albumId());

        if (albumChanging) {
            albumRepository.findById(photoRequestDTO.albumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            AlbumEntity currentAlbum = albumRepository.findById(existingPhoto.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, existingPhoto.getAlbumId()));

            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            boolean isCurrentAlbumOwner = currentAlbum.getOwnerId().equals(currentUser.getUserId());
            boolean isCurrentAlbumSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                    existingPhoto.getAlbumId(), currentUser.getUserId());

            if (!isCurrentAlbumOwner && !isCurrentAlbumSharedWithUser) {
                log.warn("[PHOTO] User {} attempted to update photo {} but is not the owner of album {} and album is not shared with them",
                        email, id, existingPhoto.getAlbumId());
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }

            if (albumChanging) {
                AlbumEntity newAlbum = albumRepository.findById(photoRequestDTO.albumId())
                        .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId()));

                boolean isNewAlbumOwner = newAlbum.getOwnerId().equals(currentUser.getUserId());
                boolean isNewAlbumSharedWithUser = albumShareRepository.existsByAlbumIdAndSharedWithUserId(
                        photoRequestDTO.albumId(), currentUser.getUserId());

                if (!isNewAlbumOwner && !isNewAlbumSharedWithUser) {
                    log.warn("[PHOTO] User {} attempted to move photo {} to album {} but is not the owner and album is not shared with them",
                            email, id, photoRequestDTO.albumId());
                    throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoRequestDTO.albumId());
                }
            }

            log.info("[PHOTO] User {} has permission to update photo {} (current album owner: {}, current album shared: {}, album changing: {})",
                    email, id, isCurrentAlbumOwner, isCurrentAlbumSharedWithUser, albumChanging);
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            AlbumEntity album = albumRepository.findById(photoEntity.getAlbumId())
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, photoEntity.getAlbumId()));

            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            if (!album.getOwnerId().equals(currentUser.getUserId())) {
                throw new DataNotFoundException(ExceptionCode.PHOTO_NOT_FOUND, id);
            }
        }

        String publicId = cloudinaryService.extractPublicIdFromUrl(photoEntity.getPath());
        if (publicId != null) {
            cloudinaryService.deleteImage(publicId);
        }

        photoRepository.deleteById(photoEntity.getPhotoId());
        log.info("[PHOTO] Deleted photo with ID: {}", id);
    }
}