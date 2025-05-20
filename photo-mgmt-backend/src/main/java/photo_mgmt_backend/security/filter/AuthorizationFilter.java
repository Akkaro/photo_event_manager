package photo_mgmt_backend.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import photo_mgmt_backend.security.util.SecurityProperties;
import photo_mgmt_backend.security.util.SecurityUtil;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> jwtToken = SecurityUtil.getTokenFromRequest(request);
        if (jwtToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = (Claims) Jwts.parser()
                .verifyWith((SecretKey) SecurityUtil.getSigningKey(securityProperties.getSecretKey()))
                .build()
                .parse(jwtToken.get())
                .getPayload();
        String email = claims.getSubject();
        Collection<SimpleGrantedAuthority> authorities = Arrays.stream(
                        ((String) claims.get(SecurityUtil.ROLE_CLAIM)).split(SecurityUtil.ROLE_DELIMITER)
                )
                .map(SimpleGrantedAuthority::new)
                .toList();
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}