package com.ecotech.api.security;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtAuthenticationConverterConfiguration {

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
               jwtGrantedAuthoritiesConverter()
        );

        return converter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>>
             jwtGrantedAuthoritiesConverter() {

                return jwt -> {
                    String role = jwt.getClaimAsString("role");

                    if (role == null || role.isBlank()) {
                        return List.of();
                    }

                    GrantedAuthority authority =
                            new SimpleGrantedAuthority("ROLE_" + role);

                    return List.of(authority);
                };
             }
    
}
