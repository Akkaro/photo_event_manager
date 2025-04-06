package photo_mgmt_backend.controller.chef;

import en.sd.chefmgmt.exception.model.ExceptionBody;
import en.sd.chefmgmt.model.dto.CollectionResponseDTO;
import en.sd.chefmgmt.model.dto.chef.ChefFilterDTO;
import en.sd.chefmgmt.model.dto.chef.ChefRequestDTO;
import en.sd.chefmgmt.model.dto.chef.ChefResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/chefs")
@Tag(name = "Chef Management", description = "Operations for managing chefs")
public interface ChefController {

    @GetMapping
    @Operation(summary = "Get all chefs", description = "Retrieve a list of chefs based on optional filtering criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of chefs retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollectionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    CollectionResponseDTO<ChefResponseDTO> findAll(@Validated ChefFilterDTO chefFilterDTO);

    @GetMapping("/{id}")
    @Operation(summary = "Get a chef by ID", description = "Retrieve a chef's details using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChefResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Chef not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR') or @authService.isSelf(#id)")
    ChefResponseDTO findById(@PathVariable(name = "id") UUID id);

    @PostMapping
    @Operation(summary = "Create a new chef", description = "Add a new chef with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chef created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChefResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    ChefResponseDTO save(@RequestBody @Valid ChefRequestDTO chefRequestDTO);

    @PutMapping("/{id}")
    @Operation(summary = "Update a chef", description = "Modify an existing chef's details using their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chef updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChefResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Chef not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or @authService.isSelf(#id)")
    ChefResponseDTO update(@PathVariable(name = "id") UUID id, @RequestBody @Valid ChefRequestDTO chefRequestDTO);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a chef", description = "Remove a chef from the system using their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chef deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Chef not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable(name = "id") UUID id);
}
