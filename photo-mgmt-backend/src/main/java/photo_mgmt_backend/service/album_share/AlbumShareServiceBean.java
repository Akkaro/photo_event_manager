package photo_mgmt_backend.service.album_share;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import photo_mgmt_backend.exception.model.DataNotFoundException;
import photo_mgmt_backend.exception.model.DuplicateDataException;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.album_share.AlbumShareResponseDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.model.entity.AlbumShareEntity;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.model.mapper.AlbumShareMapper;
import photo_mgmt_backend.repository.album.AlbumRepository;
import photo_mgmt_backend.repository.album_share.AlbumShareRepository;
import photo_mgmt_backend.repository.user.UserRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumShareServiceBean implements AlbumShareService {

    private final AlbumShareRepository albumShareRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final AlbumShareMapper albumShareMapper;

    @Override
    @Transactional
    public void shareAlbum(UUID albumId, String userEmail, UUID sharedByUserId) {
        AlbumEntity album = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        if (!album.getOwnerId().equals(sharedByUserId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        UserEntity userToShareWith = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, userEmail));

        if (userToShareWith.getUserId().equals(sharedByUserId)) {
            throw new DuplicateDataException(ExceptionCode.CONSTRAINT_VIOLATION, "Cannot share album with yourself");
        }

        if (albumShareRepository.existsByAlbumIdAndSharedWithUserId(albumId, userToShareWith.getUserId())) {
            throw new DuplicateDataException(ExceptionCode.CONSTRAINT_VIOLATION, "Album already shared with this user");
        }

        AlbumShareEntity share = new AlbumShareEntity();
        share.setAlbumId(albumId);
        share.setSharedWithUserId(userToShareWith.getUserId());
        share.setSharedByUserId(sharedByUserId);
        share.setSharedAt(ZonedDateTime.now());

        albumShareRepository.save(share);
        log.info("[ALBUM_SHARE] Album {} shared with user {}", albumId, userEmail);
    }

    @Override
    @Transactional
    public void unshareAlbum(UUID albumId, String userEmail, UUID currentUserId) {
        AlbumEntity album = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        if (!album.getOwnerId().equals(currentUserId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        UserEntity userToUnshareWith = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.USER_NOT_FOUND, userEmail));

        albumShareRepository.deleteByAlbumIdAndSharedWithUserId(albumId, userToUnshareWith.getUserId());
        log.info("[ALBUM_SHARE] Album {} unshared with user {}", albumId, userEmail);
    }

    @Override
    public List<AlbumShareResponseDTO> getAlbumShares(UUID albumId, UUID currentUserId) {
        AlbumEntity album = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId));

        if (!album.getOwnerId().equals(currentUserId)) {
            throw new DataNotFoundException(ExceptionCode.ALBUM_NOT_FOUND, albumId);
        }

        List<AlbumShareEntity> shares = albumShareRepository.findByAlbumId(albumId);
        return albumShareMapper.convertEntitiesToResponseDtos(shares);
    }

    @Override
    public boolean isAlbumSharedWithUser(UUID albumId, UUID userId) {
        return albumShareRepository.existsByAlbumIdAndSharedWithUserId(albumId, userId);
    }
}