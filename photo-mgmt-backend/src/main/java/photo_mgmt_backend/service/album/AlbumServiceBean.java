package photo_mgmt_backend.service.album;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import photo_mgmt_backend.model.dto.public_album.PublicAlbumResponseDTO;
import photo_mgmt_backend.model.dto.public_album.PublicAlbumUrlResponseDTO;
import photo_mgmt_backend.model.dto.public_album.PublicPhotoDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.entity.AlbumShareEntity;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.model.mapper.AlbumMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.album.AlbumSpec;
import photo_mgmt_backend.repository.album_share.AlbumShareRepository;
import photo_mgmt_backend.repository.photo.PhotoRepository;
import photo_mgmt_backend.repository.user.UserRepository;
import photo_mgmt_backend.service.qr.QRCodeService;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumServiceBean implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumShareRepository albumShareRepository;
    private final AlbumSpec albumSpec;
    private final AlbumMapper albumMapper;
    private final UserRepository userRepository;
    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private PhotoRepository photoRepository;
    @Override
    public CollectionResponseDTO<AlbumResponseDTO> findAll(AlbumFilterDTO filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            List<AlbumEntity> ownedAlbums = albumRepository.findByOwnerId(currentUser.getUserId());

            List<AlbumShareEntity> sharedAlbums = albumShareRepository.findBySharedWithUserId(currentUser.getUserId());
            List<AlbumEntity> sharedAlbumEntities = sharedAlbums.stream()
                    .map(AlbumShareEntity::getAlbum)
                    .toList();

            Set<AlbumEntity> allUserAlbums = new HashSet<>();
            allUserAlbums.addAll(ownedAlbums);
            allUserAlbums.addAll(sharedAlbumEntities);

            List<AlbumEntity> userAlbumsList = new ArrayList<>(allUserAlbums);

            int startIndex = filter.pageNumber() * filter.pageSize();
            int endIndex = Math.min(startIndex + filter.pageSize(), userAlbumsList.size());
            List<AlbumEntity> pagedAlbums = startIndex < userAlbumsList.size() ?
                    userAlbumsList.subList(startIndex, endIndex) : List.of();

            List<AlbumResponseDTO> albumDtos = albumMapper.convertEntitiesToResponseDtos(pagedAlbums);

            return CollectionResponseDTO.<AlbumResponseDTO>builder()
                    .pageNumber(filter.pageNumber())
                    .pageSize(filter.pageSize())
                    .totalPages((int) Math.ceil((double) userAlbumsList.size() / filter.pageSize()))
                    .totalElements(userAlbumsList.size())
                    .elements(albumDtos)
                    .build();
        }

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrModerator = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isAdminOrModerator) {
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            if (!albumEntity.getOwnerId().equals(currentUser.getUserId())) {
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String email = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, email));

            if (!albumEntity.getOwnerId().equals(currentUser.getUserId())) {
                throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, id);
            }
        }

        albumRepository.deleteById(albumEntity.getAlbumId());
    }

    private String generateQrCode(AlbumEntity album) {
        return "qr_code_" + album.getAlbumName();
    }



    @Override
    @Transactional
    public PublicAlbumUrlResponseDTO makeAlbumPublic(UUID albumId, UUID ownerId) {
        AlbumEntity albumEntity = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        if (!albumEntity.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        String publicToken;
        do {
            publicToken = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (albumRepository.existsByPublicToken(publicToken));

        albumEntity.setPublicToken(publicToken);
        albumEntity.setIsPublic(true);

        try {
            String qrCodeBase64 = qrCodeService.generateQRCodeForAlbum(publicToken);
            albumEntity.setQrCode(qrCodeBase64);
        } catch (Exception e) {
            log.error("Failed to generate QR code for album {}", albumId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }

        albumRepository.save(albumEntity);

        String publicUrl = qrCodeService.generatePublicAlbumUrl(publicToken);

        log.info("[ALBUM] Made album public: {} with token: {}", albumId, publicToken);

        return new PublicAlbumUrlResponseDTO(publicUrl, albumEntity.getQrCode());
    }

    @Override
    @Transactional
    public AlbumResponseDTO makeAlbumPrivate(UUID albumId, UUID ownerId) {
        AlbumEntity albumEntity = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        if (!albumEntity.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        albumEntity.setIsPublic(false);
        albumEntity.setPublicToken(null);
        albumEntity.setQrCode(null);

        AlbumEntity savedAlbum = albumRepository.save(albumEntity);

        log.info("[ALBUM] Made album private: {}", albumId);

        return albumMapper.convertEntityToResponseDto(savedAlbum);
    }

    @Override
    public PublicAlbumResponseDTO getPublicAlbum(String publicToken) {
        AlbumEntity albumEntity = albumRepository.findByPublicTokenAndIsPublic(publicToken, true)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, publicToken));

        List<PhotoEntity> photos = photoRepository.findByAlbumId(albumEntity.getAlbumId());

        List<PublicPhotoDTO> publicPhotos = photos.stream()
                .map(photo -> PublicPhotoDTO.builder()
                        .photoName(photo.getPhotoName())
                        .path(photo.getPath())
                        .uploadedAt(photo.getUploadedAt())
                        .isEdited(photo.getIsEdited())
                        .build())
                .toList();

        return PublicAlbumResponseDTO.builder()
                .albumName(albumEntity.getAlbumName())
                .ownerName(albumEntity.getOwner().getUserName())
                .createdAt(albumEntity.getCreatedAt())
                .photoCount(publicPhotos.size())
                .photos(publicPhotos)
                .build();
    }

    @Override
    public byte[] downloadQRCode(UUID albumId, UUID ownerId) {
        AlbumEntity albumEntity = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        // Verify ownership
        if (!albumEntity.getOwnerId().equals(ownerId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        // Check if album is public
        if (!Boolean.TRUE.equals(albumEntity.getIsPublic()) || albumEntity.getPublicToken() == null) {
            throw new IllegalStateException("Album is not public");
        }

        try {
            String publicUrl = qrCodeService.generatePublicAlbumUrl(albumEntity.getPublicToken());
            return qrCodeService.generateQRCodeBytes(publicUrl);
        } catch (Exception e) {
            log.error("Failed to generate QR code bytes for album {}", albumId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}