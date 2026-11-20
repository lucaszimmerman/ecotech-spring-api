package com.ecotech.api.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsSesConfiguration {
    
    @Bean
    SesV2Client sesV2Client(
            AwsSesProperties properties
    ) {
        return SesV2Client.builder()
                .region(
                    Region.of(properties.region())
                )
                .build();
    }
}
