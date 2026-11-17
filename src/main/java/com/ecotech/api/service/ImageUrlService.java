package com.ecotech.api.service;

import java.time.Duration;

import org.mapstruct.Named;
import org.springframework.stereotype.Service;

import com.ecotech.api.config.aws.AwsS3Properties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class ImageUrlService {

    private final S3Presigner presigner;
    private final AwsS3Properties properties;

    @Named("imageUrl")
    public String resolve(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        // Preserve legacy external images; only stored S3 keys are signed.
        if (key.startsWith("https://") || key.startsWith("http://")) {
            return key;
        }

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .responseContentDisposition("inline")
                .build();
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.presignedUrlExpirationSeconds()))
                .getObjectRequest(objectRequest)
                .build();

        return presigner.presignGetObject(request).url().toExternalForm();
    }
}
