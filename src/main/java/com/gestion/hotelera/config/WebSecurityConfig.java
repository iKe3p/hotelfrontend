package com.gestion.hotelera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Orígenes permitidos - específicos para desarrollo Angular
                .allowedOriginPatterns(
                    "http://localhost:4200",  // Angular dev server por defecto
                    "http://127.0.0.1:4200",
                    "http://localhost:3000",  // Otros posibles puertos
                    "http://localhost:*"      // Cualquier puerto localhost para flexibilidad
                )
                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                // Headers permitidos
                .allowedHeaders(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers"
                )
                // Headers expuestos
                .exposedHeaders(
                    "Authorization",
                    "Content-Type",
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials"
                )
                // Permitir credenciales (necesario para JWT)
                .allowCredentials(true)
                // Cache de preflight requests
                .maxAge(3600);
        
        // También configurar para todas las rutas si es necesario
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:4200",
                    "http://127.0.0.1:4200",
                    "http://localhost:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}