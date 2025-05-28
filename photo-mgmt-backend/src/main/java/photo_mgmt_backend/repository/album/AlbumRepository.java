package photo_mgmt_backend.repository.album;

import photo_mgmt_backend.model.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<AlbumEntity, UUID>, JpaSpecificationExecutor<AlbumEntity> {

    List<AlbumEntity> findByOwnerId(UUID ownerId);

    boolean existsByAlbumNameAndOwnerId(String albumName, UUID ownerId);

    boolean existsByAlbumNameAndOwnerIdAndAlbumIdIsNot(String albumName, UUID ownerId, UUID albumId);

    // New methods for public sharing
    Optional<AlbumEntity> findByPublicTokenAndIsPublic(String publicToken, Boolean isPublic);

    boolean existsByPublicToken(String publicToken);

    @Query("SELECT COUNT(a) FROM AlbumEntity a WHERE a.isPublic = true")
    Long countPublicAlbums();

    @Query("SELECT a FROM AlbumEntity a WHERE a.isPublic = true ORDER BY a.createdAt DESC")
    List<AlbumEntity> findPublicAlbumsOrderByCreatedAtDesc();
}