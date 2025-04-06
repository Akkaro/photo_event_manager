package photo_mgmt_backend.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import en.sd.chefmgmt.exception.model.ExceptionBody;
import en.sd.chefmgmt.exception.model.ExceptionCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointBean implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        try {
            objectMapper.writeValue(response.getWriter(), getExceptionBody(exception.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private ExceptionBody getExceptionBody(String message) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .message(message)
                .code(ExceptionCode.FORBIDDEN_ACCESS.getCode())
                .build();
    }
}