package co.com.bancodebogota.bdbapprovals.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Value("${app.security.mock-enabled:false}")
    boolean mockEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/requests/**", "/api/inbox/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/requests/**").authenticated()
                        .anyRequest().authenticated()
                );

        if (mockEnabled) {
            // MOCK por cabecera (sin Azure AD ni JwtDecoder)
            http.addFilterBefore(new MockAuthFilter(), AnonymousAuthenticationFilter.class);
            // Importante: NO configurar oauth2ResourceServer en mock para que no exija JwtDecoder
        } else {
            // Real (Azure AD) o JWT HS256 si hay JwtDecoder presente (perfil mock-jwt)
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }
        return http.build();
    }

    static class MockAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String upn = req.getHeader("X-User-UPN");
            if (upn == null || upn.isBlank()) upn = "mock.user@office365.local";

            Jwt jwt = Jwt.withTokenValue("mock")
                    .header("alg", "none")
                    .claim("upn", upn)
                    .claim("preferred_username", upn)
                    .claim("email", upn)
                    .claim("scp", List.of("Approvals.ReadWrite"))
                    .issuer("mock")
                    .subject(upn)
                    .build();

            AbstractAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(), upn);
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        }
    }

    public static String extractUpn(AbstractAuthenticationToken authentication) {
        if (authentication instanceof JwtAuthenticationToken jwt) {
            Map<String, Object> c = jwt.getToken().getClaims();
            Object v = c.getOrDefault("upn", c.getOrDefault("preferred_username", c.get("email")));
            return v != null ? v.toString() : jwt.getName();
        }
        return authentication.getName();
    }
}
