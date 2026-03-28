package blog.service.impl;

import blog.config.ObjectStorageProperties;
import blog.service.ResourceStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ResourceStorageService implements ResourceStorageService
{
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ObjectStorageProperties properties;

    @Override
    public String uploadImage(MultipartFile file)
    {
        validateConfiguration();

        String key = buildObjectKey("images", file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(resolveContentType(file))
                .build();

        try (S3Client client = createClient(); InputStream inputStream = file.getInputStream()) {
            ensureBucketExists(client);
            client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
            String accessibleUrl = buildAccessibleUrl(key);
            log.info("Uploaded object to bucket={}, key={}", properties.getBucket(), key);
            return accessibleUrl;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read upload stream", e);
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to upload object to storage", e);
        }
    }

    @Override
    public void deleteByReference(String reference)
    {
        validateConfiguration();

        String key = extractObjectKey(reference);
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Missing object reference");
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build();

        try (S3Client client = createClient()) {
            client.deleteObject(request);
            log.info("Deleted object from bucket={}, key={}", properties.getBucket(), key);
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to delete object from storage", e);
        }
    }

    private S3Client createClient()
    {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey().trim(),
                properties.getSecretKey().trim()
        );

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(resolveRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                                .build()
                );

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder.endpointOverride(URI.create(properties.getEndpoint().trim()));
        }

        return builder.build();
    }

    private void validateConfiguration()
    {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Object storage is not fully configured");
        }
    }

    private void ensureBucketExists(S3Client client)
    {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucket()).build());
        } catch (SdkException e) {
            try {
                client.createBucket(CreateBucketRequest.builder().bucket(properties.getBucket()).build());
                log.info("Created object storage bucket={}", properties.getBucket());
            } catch (SdkException createException) {
                throw new IllegalStateException("Failed to access or create object storage bucket", createException);
            }
        }
    }

    private String resolveRegion()
    {
        return StringUtils.hasText(properties.getRegion()) ? properties.getRegion().trim() : "us-east-1";
    }

    private String buildObjectKey(String folder, String originalFilename)
    {
        String extension = extractExtension(originalFilename);
        StringBuilder key = new StringBuilder();

        if (StringUtils.hasText(properties.getKeyPrefix())) {
            key.append(trimSlashes(properties.getKeyPrefix())).append('/');
        }

        key.append(folder)
                .append('/')
                .append(LocalDate.now().format(DATE_PATH))
                .append('/')
                .append(UUID.randomUUID())
                .append(extension);

        return key.toString();
    }

    private String buildAccessibleUrl(String key)
    {
        if (StringUtils.hasText(properties.getPublicUrl())) {
            return trimTrailingSlash(properties.getPublicUrl()) + "/" + key;
        }

        return trimTrailingSlash(properties.getEndpoint()) + "/" + properties.getBucket().trim() + "/" + key;
    }

    private String extractObjectKey(String reference)
    {
        if (!StringUtils.hasText(reference)) {
            return "";
        }

        String normalizedReference = reference.trim();

        if (StringUtils.hasText(properties.getPublicUrl())) {
            String publicPrefix = trimTrailingSlash(properties.getPublicUrl()) + "/";
            if (normalizedReference.startsWith(publicPrefix)) {
                return normalizedReference.substring(publicPrefix.length());
            }
        }

        if (StringUtils.hasText(properties.getEndpoint())) {
            String endpointPrefix = trimTrailingSlash(properties.getEndpoint()) + "/" + properties.getBucket().trim() + "/";
            if (normalizedReference.startsWith(endpointPrefix)) {
                return normalizedReference.substring(endpointPrefix.length());
            }
        }

        if (normalizedReference.startsWith("/")) {
            return normalizedReference.substring(1);
        }

        return normalizedReference;
    }

    private String resolveContentType(MultipartFile file)
    {
        if (StringUtils.hasText(file.getContentType())) {
            return file.getContentType();
        }
        return "application/octet-stream";
    }

    private String extractExtension(String filename)
    {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value)
    {
        String normalized = value == null ? "" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimSlashes(String value)
    {
        String normalized = value == null ? "" : value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
