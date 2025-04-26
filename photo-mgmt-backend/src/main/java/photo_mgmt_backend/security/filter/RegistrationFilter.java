package photo_mgmt_backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import photo_mgmt_backend.exception.model.DuplicateDataException;
import photo_mgmt_backend.exception.model.ExceptionBody;
import photo_mgmt_backend.exception.model.ExceptionCode;
import photo_mgmt_backend.model.dto.auth.RegisterRequestDTO;
import photo_mgmt_backend.model.dto.auth.RegisterResponseDTO;
import photo_mgmt_backend.model.entity.Role;
import photo_mgmt_backend.model.entity.UserEntity;
import photo_mgmt_backend.repository.user.UserRepository;
import photo_mgmt_backend.security.util.SecurityConstants;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Component
public class RegistrationFilter extends OncePerRequestFilter {

    private final RequestMatcher registerRequestMatcher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public RegistrationFilter(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper
    ) {
        this.registerRequestMatcher = new AntPathRequestMatcher(SecurityConstants.REGISTER_URL, "POST");
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!registerRequestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            RegisterRequestDTO registerRequest = objectMapper.readValue(
                    request.getInputStream(),
                    RegisterRequestDTO.class
            );

            // Validate passwords match (this is also handled by @PasswordMatches but we validate here as well)
            if (!registerRequest.password().equals(registerRequest.confirmPassword())) {
                writeErrorResponse(response, "Passwords do not match", ExceptionCode.VALIDATION_ERROR);
                return;
            }

            // Check if user already exists
            if (userRepository.existsByEmail(registerRequest.email())) {
                writeErrorResponse(response,
                        String.format("Email %s is already taken", registerRequest.email()),
                        ExceptionCode.CONSTRAINT_VIOLATION);
                return;
            }

            // Create new user
            UserEntity user = UserEntity.builder()
                    .userId(UUID.randomUUID())
                    .email(registerRequest.email())
                    .userName(registerRequest.userName())
                    .passwordHash(passwordEncoder.encode(registerRequest.password()))
                    .role(Role.USER)  // Default role
                    .createdAt(ZonedDateTime.now())
                    .build();

            // Save user
            UserEntity savedUser = userRepository.save(user);
            log.info("[AUTH] User {} registered successfully", savedUser.getEmail());

            // Create and write response
            RegisterResponseDTO registerResponse = new RegisterResponseDTO(
                    savedUser.getUserId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.CREATED.value());
            objectMapper.writeValue(response.getWriter(), registerResponse);

        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            writeErrorResponse(response, "Registration failed: " + e.getMessage(), ExceptionCode.SERVER_ERROR);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message, ExceptionCode exceptionCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        ExceptionBody exceptionBody = ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .message(message)
                .code(exceptionCode.getCode())
                .build();

        objectMapper.writeValue(response.getWriter(), exceptionBody);
    }
}