package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecotech.api.config.aws.AwsS3Properties;

import jakarta.validation.Validation;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

class ImageUrlServiceTest {

    private S3Presigner presigner;
    private ImageUrlService service;

    @BeforeEach
    void setUp() {
        presigner = S3Presigner.builder()
                .region(Region.SA_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                .build();
        service = new ImageUrlService(presigner, new AwsS3Properties("ecotech-images-test", "sa-east-1", 900));
    }

    @AfterEach
    void close() {
        presigner.close();
    }

    private Map<String, String> query(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(entry -> entry.split("=", 2))
                .collect(Collectors.toMap(entry -> URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                        entry -> URLDecoder.decode(entry[1], StandardCharsets.UTF_8)));
    }

    @Test
    void shouldGenerateBrowserExecutableGetUrlForConfiguredBucketAndRegion() {
        URI url = URI.create(service.resolve("posts/post-id/image.png"));

        assertThat(url.getScheme()).isEqualTo("https");
        assertThat(url.getHost()).isEqualTo("ecotech-images-test.s3.sa-east-1.amazonaws.com");
        assertThat(url.getPath()).isEqualTo("/posts/post-id/image.png");
        Map<String, String> query = query(url);
        assertThat(query).containsEntry("X-Amz-Expires", "900")
                .containsEntry("X-Amz-Algorithm", "AWS4-HMAC-SHA256")
                .containsEntry("X-Amz-SignedHeaders", "host")
                .containsEntry("response-content-disposition", "inline");
        assertThat(query.get("X-Amz-Credential")).contains("/sa-east-1/s3/aws4_request");
        assertThat(query.get("X-Amz-Signature")).hasSize(64);
        assertThat(presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(15))
                .getObjectRequest(request -> request.bucket("ecotech-images-test").key("posts/image.png"))
                .build()).isBrowserExecutable()).isTrue();
    }

    @Test
    void shouldRespectCustomExpiration() {
        ImageUrlService custom = new ImageUrlService(presigner,
                new AwsS3Properties("ecotech-images-test", "sa-east-1", 60));
        assertThat(query(URI.create(custom.resolve("users/profile.png"))))
                .containsEntry("X-Amz-Expires", "60");
    }

    @Test
    void shouldEncodeObjectKeyWithoutChangingItsMeaning() {
        String key = "posts/post-id/my image+special%20.png";
        URI url = URI.create(service.resolve(key));
        assertThat(url.getPath()).isEqualTo("/" + key);
        assertThat(url.getRawPath()).contains("my%20image%2Bspecial%2520.png");
    }

    @Test
    void shouldReturnNullForMissingImageWithoutResolvingCredentials() {
        S3Presigner unused = mock(S3Presigner.class);
        ImageUrlService serviceWithoutCredentials = new ImageUrlService(unused,
                new AwsS3Properties("ecotech-images-test", "sa-east-1", 900));
        assertThat(serviceWithoutCredentials.resolve(null)).isNull();
        assertThat(serviceWithoutCredentials.resolve("")).isNull();
        assertThat(serviceWithoutCredentials.resolve("  ")).isNull();
        verifyNoInteractions(unused);
    }

    @Test
    void shouldPreserveLegacyExternalUrlsWithoutSigningThem() {
        S3Presigner unused = mock(S3Presigner.class);
        ImageUrlService legacy = new ImageUrlService(unused,
                new AwsS3Properties("ecotech-images-test", "sa-east-1", 900));
        String url = "https://example.com/avatar.png?size=100";
        assertThat(legacy.resolve(url)).isEqualTo(url);
        assertThat(legacy.resolve("http://localhost/avatar.png")).isEqualTo("http://localhost/avatar.png");
        verifyNoInteractions(unused);
    }

    @Test
    void shouldValidateExpirationWithinS3Limits() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new AwsS3Properties("bucket", "sa-east-1", 0))).hasSize(1);
            assertThat(validator.validate(new AwsS3Properties("bucket", "sa-east-1", 604801))).hasSize(1);
            assertThat(validator.validate(new AwsS3Properties("bucket", "sa-east-1", 900))).isEmpty();
        }
    }
}
