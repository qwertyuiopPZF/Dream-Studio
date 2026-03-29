package blog.service.impl;

import blog.config.ObjectStorageProperties;
import blog.service.ResourceStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private static final String LOCAL_UPLOAD_PREFIX = "/upload/";

    private final ObjectStorageProperties properties;

    @Value("${file.upload-dir:upload}")
    private String uploadDir;

    @Override
    public String uploadResource(MultipartFile file, String folder)
    {
        String key = buildObjectKey(StringUtils.hasText(folder) ? folder.trim() : "resources", file.getOriginalFilename());

        if (!properties.isConfigured()) {
            log.warn("Object storage is not configured, storing resource locally. key={}", key);
            return storeLocally(file, key);
        }

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
        } catch (IllegalStateException | SdkException e) {
            log.warn("Object storage upload failed, falling back to local storage. bucket={}, key={}", properties.getBucket(), key, e);
            return storeLocally(file, key);
        }
    }

    @Override
    public void deleteByReference(String reference)
    {
        if (isLocalReference(reference)) {
            deleteLocal(reference);
            return;
        }

        if (!properties.isConfigured()) {
            throw new IllegalStateException("Object storage is not fully configured");
        }

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

    private String storeLocally(MultipartFile file, String key)
    {
        Path root = resolveLocalStorageRoot();
        Path target = root.resolve(key.replace("/", java.io.File.separator)).normalize();
        ensureWithinRoot(root, target);

        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored resource locally at path={}", target);
            return LOCAL_UPLOAD_PREFIX + key;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file locally", e);
        }
    }

    private void deleteLocal(String reference)
    {
        String key = extractLocalObjectKey(reference);
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Missing object reference");
        }

        Path root = resolveLocalStorageRoot();
        Path target = root.resolve(key.replace("/", java.io.File.separator)).normalize();
        ensureWithinRoot(root, target);

        try {
            Files.deleteIfExists(target);
            log.info("Deleted local resource at path={}", target);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete local object", e);
        }
    }

    private Path resolveLocalStorageRoot()
    {
        String normalizedUploadDir = StringUtils.hasText(uploadDir) ? uploadDir.trim() : "upload";
        Path root = Path.of(normalizedUploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize local upload directory", e);
        }

        return root;
    }

    private void ensureWithinRoot(Path root, Path target)
    {
        if (!target.startsWith(root)) {
            throw new IllegalStateException("Resolved local storage path is outside upload directory");
        }
    }

    private boolean isLocalReference(String reference)
    {
        if (!StringUtils.hasText(reference)) {
            return false;
        }

        String normalizedReference = reference.trim();
        return normalizedReference.startsWith(LOCAL_UPLOAD_PREFIX)
                || normalizedReference.contains(LOCAL_UPLOAD_PREFIX);
    }

    private String extractLocalObjectKey(String reference)
    {
        if (!StringUtils.hasText(reference)) {
            return "";
        }

        String normalizedReference = reference.trim();
        int prefixIndex = normalizedReference.indexOf(LOCAL_UPLOAD_PREFIX);
        if (prefixIndex >= 0) {
            return normalizedReference.substring(prefixIndex + LOCAL_UPLOAD_PREFIX.length());
        }

        if (normalizedReference.startsWith("/")) {
            return normalizedReference.substring(1);
        }

        return normalizedReference;
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

        ensurePublicReadPolicy(client);
    }

    private void ensurePublicReadPolicy(S3Client client)
    {
        String bucket = properties.getBucket().trim();
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Sid": "PublicReadGetObject",
                      "Effect": "Allow",
                      "Principal": "*",
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);

        try {
            client.putBucketPolicy(
                    PutBucketPolicyRequest.builder()
                            .bucket(bucket)
                            .policy(policy)
                            .build()
            );
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to configure public read policy for object storage bucket", e);
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
