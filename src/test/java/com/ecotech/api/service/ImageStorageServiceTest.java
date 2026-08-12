package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.ecotech.api.config.AppStorageProperties;
import com.ecotech.api.exceptions.CampoInvalidoException;

class ImageStorageServiceTest {

    @TempDir
    private Path uploadDir;

    private ImageStorageService createService() {
        return new ImageStorageService(
                new AppStorageProperties(uploadDir.toString()));
    }

    @Test
    void shouldUploadImageUsingPrefixAndGeneratedKey() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                "image".getBytes());

        String key = service.upload(file, "posts/post-id");

        assertThat(key)
                .startsWith("posts/post-id/")
                .endsWith(".png");
        assertThat(uploadDir.resolve(key)).exists()
                .hasContent("image");
    }

    @Test
    void shouldUploadJpegImageWithValidExtension() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpeg",
                "image/jpeg",
                "jpeg".getBytes());

        String key = service.upload(file, "users/user-id/profile");

        assertThat(key)
                .startsWith("users/user-id/profile/")
                .endsWith(".jpg");
        assertThat(uploadDir.resolve(key)).exists()
                .hasContent("jpeg");
    }

    @Test
    void shouldUploadWebpImageWithValidExtension() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.webp",
                "image/webp",
                "webp".getBytes());

        String key = service.upload(file, "users/user-id/cover");

        assertThat(key)
                .startsWith("users/user-id/cover/")
                .endsWith(".webp");
        assertThat(uploadDir.resolve(key)).exists()
                .hasContent("webp");
    }

    @Test
    void shouldRejectInvalidContentTypeOnUpload() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.txt",
                "text/plain",
                "content".getBytes());

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("Formato de imagem");
    }

    @Test
    void shouldRejectEmptyFileOnUpload() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                new byte[0]);

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("obrig");
    }

    @Test
    void shouldRejectFileLargerThanLimitOnUpload() {
        ImageStorageService service = createService();
        byte[] content = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                content);

        assertThatThrownBy(() -> service.upload(file, "posts/post-id"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void shouldRejectPathTraversalOnUpload() {
        ImageStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                "image".getBytes());

        assertThatThrownBy(() -> service.upload(file, "../outside"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("Caminho");
    }

    @Test
    void shouldDeleteImageWhenKeyIsPresent() throws Exception {
        ImageStorageService service = createService();
        Path image = uploadDir.resolve("posts/post-id/image.png");
        Files.createDirectories(image.getParent());
        Files.writeString(image, "image");

        service.delete("posts/post-id/image.png");

        assertThat(image).doesNotExist();
    }

    @Test
    void shouldIgnoreDeleteWhenKeyIsBlank() {
        ImageStorageService service = createService();

        service.delete(" ");

        assertThat(uploadDir).isDirectory();
    }
}
