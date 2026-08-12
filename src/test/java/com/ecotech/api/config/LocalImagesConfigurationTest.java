package com.ecotech.api.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LocalImagesConfigurationTest {

    @TempDir
    private static Path uploadDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
        registry.add("app.storage.upload-dir", () -> uploadDir.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldServeUploadedImageWithoutAuthentication() throws Exception {
        Path image = uploadDir.resolve("users/user-id/profile/avatar.png");
        Files.createDirectories(image.getParent());
        Files.writeString(image, "image");

        mockMvc.perform(get("/images/users/user-id/profile/avatar.png"))
                .andExpect(status().isOk())
                .andExpect(content().bytes("image".getBytes()));
    }
}
