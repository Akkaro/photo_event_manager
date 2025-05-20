package photo_mgmt_backend.repository.album;

import photo_mgmt_backend.model.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<AlbumEntity, UUID>, JpaSpecificationExecutor<AlbumEntity> {

    List<AlbumEntity> findByOwnerId(UUID ownerId);

    boolean existsByAlbumNameAndOwnerId(String albumName, UUID ownerId);

    boolean existsByAlbumNameAndOwnerIdAndAlbumIdIsNot(String albumName, UUID ownerId, UUID albumId);
}
