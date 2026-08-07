package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.ecotech.api.config.aws.AwsS3Properties;
import com.ecotech.api.exceptions.CampoInvalidoException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImageStorageServiceTest {

    private static final String BUCKET_NAME = "ecotech-test";

    @Mock
    private S3Client s3Client;

    private ImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new ImageStorageService(
                s3Client,
                new AwsS3Properties(BUCKET_NAME, "us-east-1"));
    }

    @Test
    void shouldUploadImageUsingPrefixAndGeneratedKey() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                "image".getBytes());

        String key = service.upload(file, "posts/post-id");

        assertThat(key)
                .startsWith("posts/post-id/")
                .endsWith(".png");

        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo(key);
        assertThat(request.contentType()).isEqualTo("image/png");
    }

    @Test
    void shouldRejectInvalidContentTypeOnUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.txt",
                "text/plain",
                "content".getBytes());

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("Formato de imagem");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldRejectEmptyFileOnUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                new byte[0]);

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("obrig");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldRejectFileLargerThanLimitOnUpload() {
        byte[] content = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                content);

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("5 MB");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldDeleteImageWhenKeyIsPresent() {
        service.delete("posts/post-id/image.png");

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);

        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo("posts/post-id/image.png");
    }

    @Test
    void shouldIgnoreDeleteWhenKeyIsBlank() {
        service.delete(" ");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
