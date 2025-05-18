package photo_mgmt_backend.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import photo_mgmt_backend.exception.model.ExceptionBody;
import photo_mgmt_backend.model.dto.auth.LoginRequestDTO;
import photo_mgmt_backend.model.dto.auth.LoginResponseDTO;
import photo_mgmt_backend.model.dto.auth.RegisterRequestDTO;
import photo_mgmt_backend.model.dto.auth.RegisterResponseDTO;

@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Operations for user authentication and login")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public interface AuthController {

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user credentials and return an authentication token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    void login(@RequestBody @Valid LoginRequestDTO loginRequestDTO);

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register user, save in db and return to login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "User already registered.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExceptionBody.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    void register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO);
}
