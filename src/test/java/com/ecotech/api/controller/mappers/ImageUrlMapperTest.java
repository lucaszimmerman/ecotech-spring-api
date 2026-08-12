package com.ecotech.api.controller.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ImageUrlMapperTest {

    private final ImageUrlMapper mapper = new ImageUrlMapper();

    @Test
    void shouldConvertImageKeyToPublicImagesPath() {
        assertThat(mapper.toPublicImageUrl("posts/post-id/image.png"))
                .isEqualTo("/images/posts/post-id/image.png");
    }

    @Test
    void shouldKeepBlankImageKey() {
        assertThat(mapper.toPublicImageUrl(null)).isNull();
        assertThat(mapper.toPublicImageUrl(" ")).isBlank();
    }

    @Test
    void shouldKeepAlreadyPublicImagesPath() {
        assertThat(mapper.toPublicImageUrl("/images/users/user-id/profile/image.png"))
                .isEqualTo("/images/users/user-id/profile/image.png");
    }
}
