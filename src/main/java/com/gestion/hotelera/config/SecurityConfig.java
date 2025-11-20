package com.gestion.hotelera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.gestion.hotelera.security.JwtAuthenticationFilter;
import java.io.IOException;
import java.util.Arrays;
import jakarta.servlet.ServletException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/habitaciones/**", "/clientes/**", "/reservas/**", "/empleados/**", "/pago/**", "/h2-console/**", "/api/**", "/logout")
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth
                // RUTAS PÚBLICAS - Incluir OPTIONS para CORS preflight
                .requestMatchers("/", "/index", "/home", "/login", "/registro", "/logout",
                                 "/css/**", "/js/**", "/images/**",
                                 "/h2-console/**", "/api/auth/**",
                                 "/habitaciones/publico", "/api/**")
                .permitAll()
                
                // Permitir OPTIONS requests para CORS preflight
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                .permitAll()

                // ADMIN
                .requestMatchers("/empleados/**", "/admin/**", "/auditoria/logs")
                .hasAuthority("ROLE_ADMIN")

                // RECEPCIONISTA o ADMIN: gestión completa de clientes
                .requestMatchers("/clientes/**", "/reservas/crear", "/habitaciones/**")
                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                // CLIENTE
                .requestMatchers("/cliente/**", "/cliente/reservas/**", "/cliente/dashboard", "/cliente/editar")
                .hasAuthority("ROLE_CLIENTE")

                // DASHBOARD (requiere login)
                .requestMatchers("/dashboard")
                .authenticated()

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(authenticationProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(content -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes específicos para desarrollo y producción
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200",  // Angular dev server
            "http://localhost:3000",  // Otros posibles puertos
            "http://127.0.0.1:4200",
            "http://localhost:*"      // Cualquier puerto localhost para flexibilidad
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciales (necesario para cookies y auth headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Aplicar CORS a todas las rutas API
        source.registerCorsConfiguration("/api/**", configuration);
        
        // También aplicar a rutas públicas si es necesario
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                                jakarta.servlet.http.HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                boolean isStaff = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> "ROLE_ADMIN".equals(a) || "ROLE_RECEPCIONISTA".equals(a));
                boolean isClient = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> "ROLE_CLIENTE".equals(a));
                        
                if (isStaff) {
                    response.sendRedirect("/dashboard?loginSuccess=true");
                } else if (isClient) {
                    response.sendRedirect("/?loginSuccess=true");
                } else {
                    response.sendRedirect("/?loginSuccess=true");
                }
            }
        };
    }
}
