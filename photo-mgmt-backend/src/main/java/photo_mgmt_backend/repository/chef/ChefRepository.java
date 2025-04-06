package photo_mgmt_backend.repository.chef;

import en.sd.chefmgmt.model.entity.ChefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChefRepository extends JpaRepository<ChefEntity, UUID>, JpaSpecificationExecutor<ChefEntity> {

    boolean existsByCnp(String cnp);

    boolean existsByCnpAndIdIsNot(String cnp, UUID id);
}
