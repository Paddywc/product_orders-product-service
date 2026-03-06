package product.orders.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http)  {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/products/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/**")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/**")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/actuator/**").permitAll()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

}
