package com.ecotech.api.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "aws.s3")
@Validated
public record AwsS3Properties(
    String bucketName,
    String region,
    @DefaultValue("900") @Min(1) @Max(604800)
    int presignedUrlExpirationSeconds
) {
    
}
