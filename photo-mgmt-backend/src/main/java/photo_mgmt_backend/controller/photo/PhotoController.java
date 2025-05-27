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
import photo_mgmt_backend.model.dto.photo_version.PhotoVersionDTO;
import photo_mgmt_backend.model.dto.photo_version.PhotoVersionHistoryDTO;
import photo_mgmt_backend.model.dto.photo_version.RevertToVersionRequestDTO;

import java.util.UUID;

@RequestMapping("/v1/photos")
@Tag(name = "Photo Management", description = "Operations for managing photos with advanced editing capabilities")
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
    @Operation(summary = "Edit a photo with advanced options",
            description = "Apply various image processing operations including brightness/contrast, gamma correction, " +
                    "histogram equalization, blur, edge detection, morphological operations, noise reduction, " +
                    "thresholding, and HSV conversion.")
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

    @PostMapping("/{id}/edit/brightness-contrast")
    @Operation(summary = "Apply brightness and contrast adjustments",
            description = "Apply simple brightness and contrast adjustments to a photo.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editBrightnessContrast(@PathVariable(name = "id") UUID photoId,
                                                @RequestParam(required = false, defaultValue = "0") Double brightness,
                                                @RequestParam(required = false, defaultValue = "1.0") Double contrast);

    @PostMapping("/{id}/edit/gamma")
    @Operation(summary = "Apply gamma correction",
            description = "Apply gamma correction to adjust image exposure.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editGamma(@PathVariable(name = "id") UUID photoId,
                                   @RequestParam Double gamma);

    @PostMapping("/{id}/edit/histogram-equalization")
    @Operation(summary = "Apply histogram equalization",
            description = "Enhance image contrast using histogram equalization.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editHistogramEqualization(@PathVariable(name = "id") UUID photoId);

    @PostMapping("/{id}/edit/blur")
    @Operation(summary = "Apply Gaussian blur",
            description = "Apply Gaussian blur with specified kernel size and sigma.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editBlur(@PathVariable(name = "id") UUID photoId,
                                  @RequestParam Integer kernelSize,
                                  @RequestParam(required = false, defaultValue = "2.0") Double sigma);

    @PostMapping("/{id}/edit/edge-detection")
    @Operation(summary = "Apply edge detection",
            description = "Apply edge detection using Canny or Sobel algorithms.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editEdgeDetection(@PathVariable(name = "id") UUID photoId,
                                           @RequestParam String type); // canny or sobel

    @PostMapping("/{id}/edit/morphological")
    @Operation(summary = "Apply morphological operations",
            description = "Apply morphological opening or closing operations.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editMorphological(@PathVariable(name = "id") UUID photoId,
                                           @RequestParam String operation, // open or close
                                           @RequestParam Integer kernelSize,
                                           @RequestParam(required = false, defaultValue = "1") Integer iterations);

    @PostMapping("/{id}/edit/denoise")
    @Operation(summary = "Apply noise reduction",
            description = "Apply noise reduction using bilateral or median filtering.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editDenoise(@PathVariable(name = "id") UUID photoId,
                                     @RequestParam String type); // bilateral or median

    @PostMapping("/{id}/edit/threshold")
    @Operation(summary = "Apply thresholding",
            description = "Apply binary thresholding with specified threshold value.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editThreshold(@PathVariable(name = "id") UUID photoId,
                                       @RequestParam Integer threshold,
                                       @RequestParam(required = false, defaultValue = "binary") String type);

    @PostMapping("/{id}/edit/auto-threshold")
    @Operation(summary = "Apply automatic thresholding",
            description = "Apply automatic thresholding using Otsu's method.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editAutoThreshold(@PathVariable(name = "id") UUID photoId);

    @PostMapping("/{id}/edit/hsv-convert")
    @Operation(summary = "Convert to HSV color space",
            description = "Convert image to HSV color space representation.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoEditResponseDTO editHsvConvert(@PathVariable(name = "id") UUID photoId);

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get photo version history",
            description = "Retrieve all versions of a photo including the original and all edits.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Version history retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoVersionHistoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoVersionHistoryDTO getVersionHistory(@PathVariable(name = "id") UUID photoId);

    @GetMapping("/{id}/original")
    @Operation(summary = "Get original image URL",
            description = "Get the URL of the original, unedited image.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Original image URL retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    String getOriginalImageUrl(@PathVariable(name = "id") UUID photoId);

    @PostMapping("/{id}/revert")
    @Operation(summary = "Revert to specific version",
            description = "Revert a photo to a specific version (0 = original, 1+ = edit versions).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo reverted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoVersionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Photo or version not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PhotoVersionDTO revertToVersion(@PathVariable(name = "id") UUID photoId,
                                    @RequestBody @Valid RevertToVersionRequestDTO request);
}