package com.ecotech.api.controller.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlMapper {

    private static final String IMAGES_PATH = "/images/";

    @Named("toPublicImageUrl")
    public String toPublicImageUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return imageKey;
        }

        if (imageKey.startsWith(IMAGES_PATH)) {
            return imageKey;
        }

        return IMAGES_PATH + imageKey.replaceFirst("^/+", "");
    }
}
