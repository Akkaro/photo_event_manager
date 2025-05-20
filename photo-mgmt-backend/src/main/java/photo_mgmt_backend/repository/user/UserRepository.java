package photo_mgmt_backend.repository.user;

import photo_mgmt_backend.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndUserIdIsNot(String email, UUID userId);

    Optional<UserEntity> findByEmail(String email);
}