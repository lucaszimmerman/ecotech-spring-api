package com.ecotech.api.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.config.aws.AwsS3Properties;
import com.ecotech.api.exceptions.CampoInvalidoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

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

    private final S3Client s3Client;
    private final AwsS3Properties properties;

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

        try {
            log.debug(
                    "Iniciando upload de imagem no S3. key={}, contentType={}, size={}",
                    key,
                    file.getContentType(),
                    file.getSize()
            );

            PutObjectRequest request =
                   PutObjectRequest.builder()
                            .bucket(properties.bucketName())
                            .key(key)
                            .contentType(file.getContentType())
                            .build();
            
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes())
        );
            log.info("Upload de imagem no S3 concluido. key={}", key);

            return key;
        } catch (IOException e) {
            log.error("Falha ao ler imagem para upload. key={}", key, e);
            throw new RuntimeException(
                    "Erro ao processar a imagem.",
                    e
            );
        } catch (S3Exception e) {
            log.error(
                    "Falha no upload de imagem para o S3. key={}, awsErrorCode={}, statusCode={}",
                    key,
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
                    e.statusCode(),
                    e
            );
            throw e;
        }
    }

    public void delete(String key){

        if (key == null || key.isBlank()) {
            return;
        }

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(properties.bucketName())
                        .key(key)
                        .build();

        try {
            s3Client.deleteObject(request);
            log.info("Imagem removida do S3. key={}", key);
        } catch (S3Exception e) {
            log.error(
                    "Falha ao remover imagem do S3. key={}, awsErrorCode={}, statusCode={}",
                    key,
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null,
                    e.statusCode(),
                    e
            );
            throw e;
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
}
