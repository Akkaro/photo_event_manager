// photo-mgmt-backend/src/main/java/photo_mgmt_backend/repository/album_share/AlbumShareSpec.java
package photo_mgmt_backend.repository.album_share;

import photo_mgmt_backend.model.dto.album_share.AlbumShareFilterDTO;
import photo_mgmt_backend.model.entity.AlbumShareEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumShareSpec extends EntitySpec<AlbumShareEntity, AlbumShareFilterDTO> {

    public AlbumShareSpec(PredicateFactory predicateFactory) {
        super(predicateFactory);
    }

    @Override
    protected List<String> getFilterableFields() {
        return List.of("albumId", "sharedWithUserId", "sharedByUserId", "sharedAt");
    }
}