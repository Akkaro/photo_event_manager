package photo_mgmt_backend.repository.photo_edit;

import photo_mgmt_backend.model.entity.PhotoEditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotoEditRepository extends JpaRepository<PhotoEditEntity, UUID>, JpaSpecificationExecutor<PhotoEditEntity> {

    List<PhotoEditEntity> findByPhotoId(UUID photoId);

    List<PhotoEditEntity> findByOwnerId(UUID ownerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PhotoEditEntity pe WHERE pe.photoId = :photoId")
    void deleteByPhotoId(@Param("photoId") UUID photoId);

    /**
     * Find all versions of a photo ordered by version number descending (newest first)
     */
    @Query("SELECT pe FROM PhotoEditEntity pe WHERE pe.photoId = :photoId ORDER BY pe.versionNumber DESC")
    List<PhotoEditEntity> findByPhotoIdOrderByVersionNumberDesc(@Param("photoId") UUID photoId);

    /**
     * Find the latest version number for a photo
     */
    @Query("SELECT MAX(pe.versionNumber) FROM PhotoEditEntity pe WHERE pe.photoId = :photoId")
    Optional<Integer> findMaxVersionNumberByPhotoId(@Param("photoId") UUID photoId);

    /**
     * Find a specific version of a photo edit
     */
    @Query("SELECT pe FROM PhotoEditEntity pe WHERE pe.photoId = :photoId AND pe.versionNumber = :versionNumber")
    Optional<PhotoEditEntity> findByPhotoIdAndVersionNumber(@Param("photoId") UUID photoId, @Param("versionNumber") Integer versionNumber);

    /**
     * Get the latest version (current version) of a photo
     */
    @Query("SELECT pe FROM PhotoEditEntity pe WHERE pe.photoId = :photoId AND pe.versionNumber = (SELECT MAX(pe2.versionNumber) FROM PhotoEditEntity pe2 WHERE pe2.photoId = :photoId)")
    Optional<PhotoEditEntity> findLatestVersionByPhotoId(@Param("photoId") UUID photoId);

    /**
     * Count total versions for a photo
     */
    @Query("SELECT COUNT(pe) FROM PhotoEditEntity pe WHERE pe.photoId = :photoId")
    Long countVersionsByPhotoId(@Param("photoId") UUID photoId);
}