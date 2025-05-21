package photo_mgmt_backend.repository.album;

import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.entity.AlbumEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumSpec extends EntitySpec<AlbumEntity, AlbumFilterDTO> {

    public AlbumSpec(PredicateFactory predicateFactory) {
        super(predicateFactory);
    }

    @Override
    protected List<String> getFilterableFields() {
        return List.of("albumName", "ownerId", "createdAt");
    }
}