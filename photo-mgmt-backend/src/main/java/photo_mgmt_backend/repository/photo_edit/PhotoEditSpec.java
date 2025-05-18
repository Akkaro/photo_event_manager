package photo_mgmt_backend.repository.photo_edit;

import photo_mgmt_backend.model.dto.photo_edit.PhotoEditFilterDTO;
import photo_mgmt_backend.model.entity.PhotoEditEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhotoEditSpec extends EntitySpec<PhotoEditEntity, PhotoEditFilterDTO> {

    public PhotoEditSpec(PredicateFactory predicateFactory) {
        super(predicateFactory);
    }

    @Override
    protected List<String> getFilterableFields() {
        return List.of("photoId", "ownerId", "editedAt", "brightness", "contrast");
    }
}