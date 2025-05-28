package photo_mgmt_backend.controller.album;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import photo_mgmt_backend.exception.model.ExceptionBody;
import photo_mgmt_backend.model.dto.CollectionResponseDTO;
import photo_mgmt_backend.model.dto.album.AlbumFilterDTO;
import photo_mgmt_backend.model.dto.album.AlbumRequestDTO;
import photo_mgmt_backend.model.dto.album.AlbumResponseDTO;
import photo_mgmt_backend.model.dto.album_share.AlbumShareResponseDTO;
import photo_mgmt_backend.model.dto.album_share.ShareAlbumRequestDTO;
import photo_mgmt_backend.model.dto.album_share.UnshareAlbumRequestDTO;
import photo_mgmt_backend.model.dto.public_album.PublicAlbumUrlResponseDTO;

import java.util.List;
import java.util.UUID;

@RequestMapping("/v1/albums")
@Tag(name = "Album Management", description = "Operations for managing photo albums")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public interface AlbumController {

    @GetMapping
    @Operation(summary = "Get all albums", description = "Retrieve a list of albums based on optional filtering criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of albums retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollectionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    CollectionResponseDTO<AlbumResponseDTO> findAll(@Validated AlbumFilterDTO albumFilterDTO);

    @GetMapping("/{id}")
    @Operation(summary = "Get an album by ID", description = "Retrieve an album's details using its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Album found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlbumResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    AlbumResponseDTO findById(@PathVariable(name = "id") UUID id);

    @PostMapping
    @Operation(summary = "Create a new album", description = "Add a new album with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Album created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlbumResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    AlbumResponseDTO save(@RequestBody @Valid AlbumRequestDTO albumRequestDTO);

    @PutMapping("/{id}")
    @Operation(summary = "Update an album", description = "Modify an existing album's details using its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Album updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlbumResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    AlbumResponseDTO update(@PathVariable(name = "id") UUID id, @RequestBody @Valid AlbumRequestDTO albumRequestDTO);

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an album", description = "Remove an album from the system using its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Album deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    void delete(@PathVariable(name = "id") UUID id);

    @PostMapping("/{albumId}/share")
    @Operation(summary = "Share album with user")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    void shareAlbum(@PathVariable UUID albumId, @RequestBody @Valid ShareAlbumRequestDTO request);

    @DeleteMapping("/{albumId}/unshare")
    @Operation(summary = "Unshare album with user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    void unshareAlbum(@PathVariable UUID albumId, @RequestBody @Valid UnshareAlbumRequestDTO request);

    @GetMapping("/{albumId}/shares")
    @Operation(summary = "Get users album is shared with")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    List<AlbumShareResponseDTO> getAlbumShares(@PathVariable UUID albumId);

    @PostMapping("/{albumId}/public")
    @Operation(summary = "Make album public", description = "Make an album publicly accessible and generate QR code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Album made public successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicAlbumUrlResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    PublicAlbumUrlResponseDTO makeAlbumPublic(@PathVariable UUID albumId);

    @DeleteMapping("/{albumId}/public")
    @Operation(summary = "Make album private", description = "Remove public access from an album.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Album made private successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlbumResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    AlbumResponseDTO makeAlbumPrivate(@PathVariable UUID albumId);

    @GetMapping("/{albumId}/qr-code")
    @Operation(summary = "Download QR code", description = "Download QR code image for a public album.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR code downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Album not found or not public",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<byte[]> downloadQRCode(@PathVariable UUID albumId);
}