package com.authServer.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.authServer.configuration.filter.JWTTokenValidatorFilter;
import com.authServer.shared.tools.ErrorHTTPRes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final JWTTokenValidatorFilter jwtTokenValidatorFilter;
    private ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(
            JWTTokenValidatorFilter jwtTokenValidatorFilter
    ){
        this.jwtTokenValidatorFilter = jwtTokenValidatorFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorHTTPRes res = ErrorHTTPRes.builder()
                    .timestamp(LocalDateTime.now().toString())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Invalid Credentials")
                    .path(request.getRequestURI())
                    .build();
            objectMapper.writeValue(response.getOutputStream(), res);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorHTTPRes res = ErrorHTTPRes.builder()
                    .timestamp(LocalDateTime.now().toString())
                    .status(HttpStatus.FORBIDDEN.value())
                    .error("Forbidden")
                    .message("Access Denied")
                    .path(request.getRequestURI())
                    .build();
            objectMapper.writeValue(response.getOutputStream(), res);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Restrict to React app origin
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Explicitly list allowed methods (OPTIONS excluded as requested, but might be needed for preflight)
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        // Allow necessary headers for JWT and JSON
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Expose Authorization header for frontend to read JWT
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        // Allow credentials if needed by frontend clients
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
}