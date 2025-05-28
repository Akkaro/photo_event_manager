package photo_mgmt_backend.repository.photo;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.entity.PhotoEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    @Override
    public Specification<PhotoEntity> createSpecification(PhotoFilterDTO filter) {
        return (Root<PhotoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.albumId() != null) {
                predicates.add(cb.equal(root.get("albumId"), filter.albumId()));
            }

            if (filter.ownerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), filter.ownerId()));
            }

            if (filter.isEdited() != null) {
                predicates.add(cb.equal(root.get("isEdited"), filter.isEdited()));
            }

            if (filter.uploadedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("uploadedAt"), filter.uploadedAt()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}