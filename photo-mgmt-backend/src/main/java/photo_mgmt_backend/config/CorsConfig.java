package photo_mgmt_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from your frontend - both authenticated and public routes
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (important for authentication)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply CORS to all endpoints
        source.registerCorsConfiguration("/**", configuration);

        // Special configuration for public endpoints (no credentials needed)
        CorsConfiguration publicConfiguration = new CorsConfiguration();
        publicConfiguration.setAllowedOrigins(Arrays.asList("*")); // Allow all origins for public content
        publicConfiguration.setAllowedMethods(Arrays.asList("GET", "OPTIONS"));
        publicConfiguration.setAllowedHeaders(Arrays.asList("*"));
        publicConfiguration.setAllowCredentials(false); // Public endpoints don't need credentials

        // Apply to public routes specifically
        source.registerCorsConfiguration("/api/v1/public/**", publicConfiguration);

        return source;
    }
}