package com.locuspark.api.config;

import com.locuspark.api.security.SecurityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()

                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui", "/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources", "/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // --- COMPANIES ---
                        .requestMatchers(HttpMethod.POST, "/companies").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/companies").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/companies/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/companies/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/companies/*").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // --- USERS ---
                        .requestMatchers(HttpMethod.POST, "/users/company/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/company/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/role").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // --- CONFIGURATIONS (tariff + pricing) ---
                        .requestMatchers(HttpMethod.PUT, "/configurations/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/configurations/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // --- PARTNERSHIPS ---
                        .requestMatchers(HttpMethod.POST, "/partnerships").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/partnerships/*").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/partnerships/*").hasAnyRole("SUPER_ADMIN", "ADMIN")

                        // --- REPORTS ---
                        .requestMatchers(HttpMethod.GET, "/reports", "/reports/export").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://locus-park.vercel.app", "http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}