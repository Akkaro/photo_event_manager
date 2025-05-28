package photo_mgmt_backend.controller.public_album;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import photo_mgmt_backend.exception.model.ExceptionBody;
import photo_mgmt_backend.model.dto.public_album.PublicAlbumResponseDTO;
import photo_mgmt_backend.service.album.AlbumService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/public")
@Tag(name = "Public Album Access", description = "Public access to shared albums (no authentication required)")
@CrossOrigin(origins = "*") // Allow public access from any origin
public class PublicAlbumController {

    private final AlbumService albumService;

    @GetMapping("/album/{publicToken}")
    @Operation(summary = "Get public album", description = "Retrieve a publicly shared album using its public token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public album retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PublicAlbumResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Album not found or not public",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    public PublicAlbumResponseDTO getPublicAlbum(@PathVariable String publicToken) {
        log.info("[PUBLIC] Accessing public album with token: {}", publicToken);
        return albumService.getPublicAlbum(publicToken);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint for public access.")
    @ResponseStatus(HttpStatus.OK)
    public String health() {
        return "Public API is healthy";
    }
}