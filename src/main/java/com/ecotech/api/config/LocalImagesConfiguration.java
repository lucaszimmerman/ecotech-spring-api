package com.ecotech.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LocalImagesConfiguration implements WebMvcConfigurer {

    private final AppStorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = storageProperties.uploadPath()
                .toUri()
                .toString();

        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadLocation);
    }
}
