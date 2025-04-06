package photo_mgmt_backend.repository.user;

import photo_mgmt_backend.model.dto.user.UserFilterDTO;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.repository.spec.EntitySpec;
import photo_mgmt_backend.repository.spec.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserSpec extends EntitySpec<UserEntity, UserFilterDTO> {

    public UserSpec(PredicateFactory predicateFactory) {
        super(predicateFactory);
    }

    @Override
    protected List<String> getFilterableFields() {
        return List.of("userName", "email", "userType", "createdAt");
    }
}