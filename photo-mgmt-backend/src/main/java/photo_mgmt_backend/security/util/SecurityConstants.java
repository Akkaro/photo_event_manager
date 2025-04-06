package photo_mgmt_backend.security.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SecurityConstants {

    public static final Integer PASSWORD_STRENGTH = 10;
    public static final String LOGIN_URL = "/v1/auth/login";
    public static final String JWT_TOKEN = "jwt-token";
    public static final String AUTH_PATHS_TO_SKIP = "/v1/auth/**";
    public static final String[] SWAGGER_PATHS_TO_SKIP = {
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };
}