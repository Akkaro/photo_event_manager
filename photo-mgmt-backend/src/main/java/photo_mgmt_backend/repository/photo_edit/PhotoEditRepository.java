package photo_mgmt_backend.repository.photo_edit;

import photo_mgmt_backend.model.entity.PhotoEditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhotoEditRepository extends JpaRepository<PhotoEditEntity, UUID>, JpaSpecificationExecutor<PhotoEditEntity> {

    List<PhotoEditEntity> findByPhotoId(UUID photoId);

    List<PhotoEditEntity> findByOwnerId(UUID ownerId);

    void deleteByPhotoId(UUID photoId);
}