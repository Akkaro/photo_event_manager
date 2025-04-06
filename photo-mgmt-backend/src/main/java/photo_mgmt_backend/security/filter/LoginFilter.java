package photo_mgmt_backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import en.sd.chefmgmt.exception.model.ExceptionBody;
import en.sd.chefmgmt.exception.model.ExceptionCode;
import en.sd.chefmgmt.model.dto.auth.LoginRequestDTO;
import en.sd.chefmgmt.security.util.SecurityConstants;
import en.sd.chefmgmt.security.util.SecurityProperties;
import en.sd.chefmgmt.security.util.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public LoginFilter(
            AuthenticationManager authenticationManager,
            SecurityProperties securityProperties,
            ObjectMapper objectMapper
    ) {
        super(authenticationManager);
        setFilterProcessesUrl(SecurityConstants.LOGIN_URL);
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            LoginRequestDTO authenticationRequest = objectMapper.readValue(
                    request.getInputStream(),
                    LoginRequestDTO.class
            );
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticationRequest.email(),
                    authenticationRequest.password()
            );

            return getAuthenticationManager().authenticate(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());

            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) {
        String accessToken = SecurityUtil.generateJwtToken(
                ((User) authResult.getPrincipal()).getUsername(),
                authResult.getAuthorities(),
                securityProperties.getTokenExpirationDays(),
                securityProperties.getSecretKey()
        );
        response.addCookie(SecurityUtil.buildCookie(
                SecurityConstants.JWT_TOKEN,
                accessToken,
                securityProperties.getTokenExpirationDays()
        ));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        objectMapper.writeValue(response.getWriter(), getExceptionBody(failed.getMessage()));
    }

    private ExceptionBody getExceptionBody(String message) {
        return ExceptionBody.builder()
                .timestamp(ZonedDateTime.now())
                .message(message)
                .code(ExceptionCode.INVALID_CREDENTIALS.getCode())
                .build();
    }
}