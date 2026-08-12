package com.ecotech.api.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.config.AppStorageProperties;
import com.ecotech.api.exceptions.CampoInvalidoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final AppStorageProperties storageProperties;

    public String upload(
        MultipartFile file,
        String keyPrefix
    ){
        validate(file);

        String extension = getExtension(file);

        String key =
                keyPrefix 
                + "/"
                + UUID.randomUUID()
                + extension;
        Path uploadRoot = storageProperties.uploadPath();
        Path target = resolveInsideUploadRoot(uploadRoot, key);

        try {
            log.debug(
                    "Iniciando upload de imagem no filesystem local. key={}, contentType={}, size={}",
                    key,
                    file.getContentType(),
                    file.getSize()
            );

            Files.createDirectories(target.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Upload de imagem no filesystem local concluido. key={}", key);

            return key;
        } catch (IOException e) {
            log.error("Falha ao ler imagem para upload. key={}", key, e);
            throw new RuntimeException(
                    "Erro ao processar a imagem.",
                    e
            );
        }
    }

    public void delete(String key){

        if (key == null || key.isBlank()) {
            return;
        }

        Path uploadRoot = storageProperties.uploadPath();
        Path target = resolveInsideUploadRoot(uploadRoot, key);

        try {
            Files.deleteIfExists(target);
            log.info("Imagem removida do filesystem local. key={}", key);
        } catch (IOException e) {
            log.error("Falha ao remover imagem do filesystem local. key={}", key, e);
            throw new RuntimeException(
                    "Erro ao remover a imagem.",
                    e
            );
        }
    }

    
    private void validate(MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            log.warn("Upload de imagem recusado: arquivo ausente ou vazio.");
            throw new CampoInvalidoException(
                "file",
                "A imagem é obrigatória."
        );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
             log.warn("Upload de imagem recusado: tamanho excedido. size={}", file.getSize());
             throw new CampoInvalidoException(
                "file",
                "A imagem deve possuir no máximo 5 MB."
        );
        }

        if (!ALLOWED_CONTENT_TYPES.contains(
            file.getContentType()
        )) {
            log.warn("Upload de imagem recusado: contentType nao permitido. contentType={}", file.getContentType());
            throw new CampoInvalidoException(
                "file",
                "Formato de imagem não permitido. Utilize JPEG, PNG ou WebP."
        );
        }
    }
    
    private String getExtension(MultipartFile file) {
        
        String contentType = file.getContentType();

        return switch (contentType){
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private Path resolveInsideUploadRoot(Path uploadRoot, String relativePath) {
        Path target = uploadRoot.resolve(relativePath)
                .normalize();

        if (!target.startsWith(uploadRoot)) {
            log.warn("Caminho de imagem recusado por path traversal. path={}", relativePath);
            throw new CampoInvalidoException(
                    "file",
                    "Caminho de armazenamento invalido."
            );
        }

        return target;
    }
}
