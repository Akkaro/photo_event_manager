package photo_mgmt_backend.controller.chef;

import en.sd.chefmgmt.model.dto.CollectionResponseDTO;
import en.sd.chefmgmt.model.dto.chef.ChefFilterDTO;
import en.sd.chefmgmt.model.dto.chef.ChefRequestDTO;
import en.sd.chefmgmt.model.dto.chef.ChefResponseDTO;
import en.sd.chefmgmt.service.chef.ChefService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChefControllerBean implements ChefController {

    private final ChefService chefService;

    @Override
    public CollectionResponseDTO<ChefResponseDTO> findAll(ChefFilterDTO chefFilterDTO) {
        log.info("[CHEF] Finding all chefs: {}", chefFilterDTO);

        return chefService.findAll(chefFilterDTO);
    }

    @Override
    public ChefResponseDTO findById(UUID id) {
        log.info("[CHEF] Finding chef by id: {}", id);

        return chefService.findById(id);
    }

    @Override
    public ChefResponseDTO save(ChefRequestDTO chefRequestDTO) {
        log.info("[CHEF] Saving chef: {}", chefRequestDTO);

        return chefService.save(chefRequestDTO);
    }

    @Override
    public ChefResponseDTO update(UUID id, ChefRequestDTO chefRequestDTO) {
        log.info("[CHEF] Updating chef: {}", chefRequestDTO);

        return chefService.update(id, chefRequestDTO);
    }

    @Override
    public void delete(UUID id) {
        log.info("[CHEF] Deleting chef: {}", id);

        chefService.delete(id);
    }
}
