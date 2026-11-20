package com.ecotech.api.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.ses")
public record AwsSesProperties(
    String fromEmail,
    String region
) {
    
}
