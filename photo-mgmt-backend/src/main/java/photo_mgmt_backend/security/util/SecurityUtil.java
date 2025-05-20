package photo_mgmt_backend.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@UtilityClass
public class SecurityUtil {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ROLE_CLAIM = "role";
    public static final String ROLE_DELIMITER = ",";

    public static String generateJwtToken(
            String email,
            Collection<? extends GrantedAuthority> roles,
            Integer tokenExpirationDays,
            String secretKey
    ) {
        return Jwts.builder()
                .subject(email)
                .claim(ROLE_CLAIM, String.join(ROLE_DELIMITER, roles.stream().map(GrantedAuthority::getAuthority).toList()))
                .expiration(SecurityUtil.getExpirationDate(tokenExpirationDays))
                .signWith(SecurityUtil.getSigningKey(secretKey))
                .compact();
    }

    public static Cookie buildCookie(String cookieName, String cookieValue, Integer tokenExpirationDays) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge((int) SecurityUtil.getExpirationDate(tokenExpirationDays).getTime());

        return cookie;
    }

    public static Optional<String> getTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.substring(TOKEN_PREFIX.length()));
    }

    public static Date getExpirationDate(Integer tokenExpirationDays) {
        return Date.valueOf(LocalDate.now().plusDays(tokenExpirationDays));
    }

    public static Key getSigningKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}