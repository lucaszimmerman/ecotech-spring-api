package com.ecotech.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostImageService {

    private static final String POST_IMAGE_PREFIX = "posts/";

    private final ImageStorageService imageStorageService;

    public void uploadImage(Post post, MultipartFile file) {
        if (!hasFile(file)) {
            return;
        }

        post.setImageUrl(imageStorageService.upload(file, imagePrefix(post)));
    }

    public String prepareImageUpdate(
            Post post,
            MultipartFile file,
            boolean removeImage) {
        validateImageUpdate(file, removeImage);

        if (hasFile(file)) {
            return replaceImage(post, file);
        }

        if (removeImage) {
            return removeImage(post);
        }

        return null;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            imageStorageService.delete(imageUrl);
        }
    }

    private void validateImageUpdate(MultipartFile file, boolean removeImage) {
        if (removeImage && hasFile(file)) {
            throw new CampoInvalidoException(
                    "removeImage",
                    "Nao e permitido remover e enviar uma nova imagem na mesma requisicao.");
        }
    }

    private String replaceImage(Post post, MultipartFile file) {
        String previousImageUrl = post.getImageUrl();
        String newImageUrl = imageStorageService.upload(file, imagePrefix(post));

        post.setImageUrl(newImageUrl);

        return previousImageUrl;
    }

    private String removeImage(Post post) {
        String previousImageUrl = post.getImageUrl();

        post.setImageUrl(null);

        return previousImageUrl;
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String imagePrefix(Post post) {
        return POST_IMAGE_PREFIX + post.getId();
    }
}
