package com.campusbloom.backend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> origins = Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList();

                if (origins.size() == 1 && "*".equals(origins.get(0))) {
                    registry.addMapping("/**")
                            .allowedOriginPatterns("*")
                            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                            .allowedHeaders("*");
                    return;
                }

                registry.addMapping("/**")
                        .allowedOrigins(origins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
