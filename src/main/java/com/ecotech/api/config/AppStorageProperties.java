package com.ecotech.api.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record AppStorageProperties(
        String uploadDir) {

    public Path uploadPath() {
        String configuredUploadDir = uploadDir == null || uploadDir.isBlank()
                ? "uploads"
                : uploadDir;

        return Path.of(configuredUploadDir)
                .toAbsolutePath()
                .normalize();
    }
}
