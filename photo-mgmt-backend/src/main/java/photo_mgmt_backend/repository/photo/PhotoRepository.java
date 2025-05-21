package photo_mgmt_backend.repository.photo;

import photo_mgmt_backend.model.entity.PhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID>, JpaSpecificationExecutor<PhotoEntity> {

    List<PhotoEntity> findByAlbumId(UUID albumId);

    List<PhotoEntity> findByOwnerId(UUID ownerId);

    long countByAlbumId(UUID albumId);
}
