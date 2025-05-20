package photo_mgmt_backend.repository.spec.predicate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Optional;

public interface PredicateStrategy<Type> {

    Optional<Predicate> createPredicate(String field, Type value, Root<?> root, CriteriaBuilder criteriaBuilder);
}