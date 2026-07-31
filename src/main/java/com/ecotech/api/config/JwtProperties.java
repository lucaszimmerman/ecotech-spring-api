package com.ecotech.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
     String secretKey,
     long expiration,
     String issuer
) {
    
}
