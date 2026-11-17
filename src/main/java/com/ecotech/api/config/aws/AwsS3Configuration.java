package com.ecotech.api.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Configuration {

    @Bean
    S3Presigner s3Presigner(AwsS3Properties properties) {
        return S3Presigner.builder()
                .region(Region.of(properties.region()))
                .build();
    }
    
    @Bean
    S3Client s3Client(AwsS3Properties properties){

        return S3Client.builder()
                 .region(Region.of(properties.region()))
                 .build();
    }
}
