package com.ecotech.api.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;


@Configuration
public class JwtConfiguration {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Bean
    SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        byte[] keyBytes = Base64
                .getDecoder()
                .decode(jwtProperties.secretKey());

        return new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(
        SecretKey secretKey,
         JwtProperties jwtProperties
        ) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> validator =
                JwtValidators.createDefaultWithIssuer(
                    jwtProperties.issuer()
                );

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }
}