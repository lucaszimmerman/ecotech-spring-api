package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Post;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private PostImageService service;

    private Post createPost() {
        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setContent("Conteudo valido do post.");

        return post;
    }

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile(
                "file",
                "post.png",
                "image/png",
                "image".getBytes());
    }

    @Test
    void shouldUploadPostImageWhenFileIsInformed() {
        Post post = createPost();
        MockMultipartFile file = createImageFile();
        String imageUrl = "posts/" + post.getId() + "/new.png";

        when(imageStorageService.upload(file, "posts/" + post.getId()))
                .thenReturn(imageUrl);

        service.uploadImage(post, file);

        assertThat(post.getImageUrl()).isEqualTo(imageUrl);

        verify(imageStorageService).upload(file, "posts/" + post.getId());
    }

    @Test
    void shouldKeepPostImageWhenNoImageUpdateIsRequested() {
        Post post = createPost();
        String oldImageUrl = "posts/" + post.getId() + "/old.png";
        post.setImageUrl(oldImageUrl);

        String imageUrlToDelete = service.prepareImageUpdate(post, null, false);

        assertThat(post.getImageUrl()).isEqualTo(oldImageUrl);
        assertThat(imageUrlToDelete).isNull();

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void shouldPreparePostImageReplacementAfterNewUploadSuccessfully() {
        Post post = createPost();
        MockMultipartFile file = createImageFile();
        String oldImageUrl = "posts/" + post.getId() + "/old.png";
        String newImageUrl = "posts/" + post.getId() + "/new.png";
        post.setImageUrl(oldImageUrl);

        when(imageStorageService.upload(file, "posts/" + post.getId()))
                .thenReturn(newImageUrl);

        String imageUrlToDelete = service.prepareImageUpdate(post, file, false);

        assertThat(post.getImageUrl()).isEqualTo(newImageUrl);
        assertThat(imageUrlToDelete).isEqualTo(oldImageUrl);

        verify(imageStorageService).upload(file, "posts/" + post.getId());
        verify(imageStorageService, never()).delete(oldImageUrl);
    }

    @Test
    void shouldPreparePostImageRemovalWhenRequestedWithoutFile() {
        Post post = createPost();
        String oldImageUrl = "posts/" + post.getId() + "/old.png";
        post.setImageUrl(oldImageUrl);

        String imageUrlToDelete = service.prepareImageUpdate(post, null, true);

        assertThat(post.getImageUrl()).isNull();
        assertThat(imageUrlToDelete).isEqualTo(oldImageUrl);

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void shouldThrowExceptionWhenRemoveImageAndFileAreInformedTogether() {
        Post post = createPost();
        MockMultipartFile file = createImageFile();

        assertThatThrownBy(() -> service.prepareImageUpdate(post, file, true))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("remover")
                .hasMessageContaining("nova imagem");

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void shouldDeleteImageWhenImageUrlIsPresent() {
        String imageUrl = "posts/" + UUID.randomUUID() + "/image.png";

        service.deleteImage(imageUrl);

        verify(imageStorageService).delete(imageUrl);
    }

    @Test
    void shouldIgnoreDeleteWhenImageUrlIsBlank() {
        service.deleteImage(" ");

        verifyNoInteractions(imageStorageService);
    }
}
