package co.com.bancodebogota.bdbapprovals.infrastructure.security;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("mock-auth")
public class SecurityConfigMock {

    private static final String TEST_SECRET = "bdb-approvals-mock-secret-32bytes-minimo!!";

    @Bean
    JwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(TEST_SECRET.getBytes(), "HmacSHA256");
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setClaimSetConverter(MappedJwtClaimSetConverter.withDefaults(null));
        return decoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
