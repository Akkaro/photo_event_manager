package photo_mgmt_backend.controller.photo;

import org.springframework.web.multipart.MultipartFile;
import photo_mgmt_backend.exception.model.ExceptionBody;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.photo.PhotoFilterDTO;
import photo_mgmt_backend.model.dto.photo.PhotoRequestDTO;
import photo_mgmt_backend.model.dto.photo.PhotoResponseDTO;
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
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditRequestDTO;
import photo_mgmt_backend.model.dto.photo_edit.PhotoEditResponseDTO;

import java.util.UUID;

@RequestMapping("/v1/photos")
@Tag(name = "Photo Management", description = "Operations for managing photos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public interface PhotoController {

    @GetMapping
    @Operation(summary = "Get all photos", description = "Retrieve a list of photos based on optional filtering criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of photos retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollectionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    CollectionResponseDTO<PhotoResponseDTO> findAll(@Validated PhotoFilterDTO photoFilterDTO);

    @GetMapping("/{id}")
    @Operation(summary = "Get a photo by ID", description = "Retrieve a photo's details using its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoResponseDTO findById(@PathVariable(name = "id") UUID id);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new photo", description = "Add a new photo with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photo uploaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    PhotoResponseDTO save(@RequestPart("photo") String photoJson, @RequestPart("file") MultipartFile file);

    @PutMapping("/{id}")
    @Operation(summary = "Update a photo", description = "Modify an existing photo's details using its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoResponseDTO update(@PathVariable(name = "id") UUID id, @RequestBody @Valid PhotoRequestDTO photoRequestDTO);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a photo", description = "Remove a photo from the system using its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    void delete(@PathVariable(name = "id") UUID id);

    @PostMapping("/{id}/edit")
    @Operation(summary = "Edit a photo", description = "Apply brightness and contrast adjustments to a photo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo edited successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoEditResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editPhoto(@PathVariable(name = "id") UUID photoId,
                                   @RequestBody @Valid PhotoEditRequestDTO editRequest);
}