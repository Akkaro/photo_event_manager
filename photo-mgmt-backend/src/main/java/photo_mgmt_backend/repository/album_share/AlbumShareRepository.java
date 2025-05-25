package photo_mgmt_backend.repository.album_share;

import photo_mgmt_backend.model.entity.AlbumShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumShareRepository extends JpaRepository<AlbumShareEntity, UUID> {
    List<AlbumShareEntity> findBySharedWithUserId(UUID userId);
    List<AlbumShareEntity> findByAlbumId(UUID albumId);
    boolean existsByAlbumIdAndSharedWithUserId(UUID albumId, UUID sharedWithUserId);
    void deleteByAlbumIdAndSharedWithUserId(UUID albumId, UUID sharedWithUserId);
}
