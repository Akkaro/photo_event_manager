package photo_mgmt_backend.repository.photo;

import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhotoSpec extends EntitySpec<PhotoEntity, PhotoFilterDTO> {

    public PhotoSpec(PredicateFactory predicateFactory) {
        super(predicateFactory);
    }

    @Override
    protected List<String> getFilterableFields() {
        return List.of("albumId", "ownerId", "uploadedAt", "isEdited");
    }
}