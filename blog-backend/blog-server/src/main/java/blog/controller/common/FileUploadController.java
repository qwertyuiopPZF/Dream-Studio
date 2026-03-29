package blog.controller.common;

import blog.dto.ArticleDTO;
import blog.result.Result;
import blog.service.ArticleService;
import blog.service.ResourceStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
@Api(tags = "Upload")
@CrossOrigin
@RequiredArgsConstructor
public class FileUploadController
{
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_MARKDOWN_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    private final ResourceStorageService resourceStorageService;
    private final ArticleService articleService;

    @PostMapping("/images")
    @ApiOperation("Upload image resource")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.isEmpty()) {
            return Result.error("Please select an image file");
        }

        if (!isSupportedImage(file)) {
            return Result.error("Only common image formats are supported");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error("Image size must be less than 10MB");
        }

        try {
            String imageUrl = resourceStorageService.uploadImage(file);
            return Result.success("上传成功", imageUrl);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Upload image failed: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/markdown")
    @ApiOperation("Import markdown file")
    public Result<Map<String, Object>> uploadMarkdown(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.isEmpty()) {
            return Result.error("Please select a markdown file");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return Result.error("Missing original file name");
        }

        String extension = getFileExtension(originalFilename);
        if (!"md".equalsIgnoreCase(extension) && !"markdown".equalsIgnoreCase(extension)) {
            return Result.error("Only .md or .markdown files are supported");
        }

        if (file.getSize() > MAX_MARKDOWN_SIZE) {
            return Result.error("Markdown file size must be less than 10MB");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String title = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String summary = generateSummary(content, 200);

            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setTitle(title);
            articleDTO.setContent(content);
            articleDTO.setSummary(summary);
            articleDTO.setStatus(0);
            articleDTO.setIsComment(1);

            Long articleId = articleService.saveArticle(articleDTO);

            Map<String, Object> payload = new HashMap<>();
            payload.put("articleId", articleId);
            payload.put("title", title);
            payload.put("summary", summary);
            payload.put("content", content);
            payload.put("contentLength", content.length());
            payload.put("status", 0);

            return Result.success(payload);
        } catch (IOException e) {
            log.error("Read markdown file failed", e);
            return Result.error("Failed to read markdown file");
        } catch (Exception e) {
            log.error("Create article from markdown failed", e);
            return Result.error("Failed to import markdown file");
        }
    }

    @DeleteMapping("/images")
    @ApiOperation("Delete uploaded image by url or key")
    public Result<Void> deleteImage(@RequestParam("url") String url)
    {
        try {
            resourceStorageService.deleteByReference(url);
            return Result.success();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Delete image failed: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/images/{filename}")
    @ApiOperation("Delete uploaded image by object key")
    public Result<Void> deleteImageByFilename(@PathVariable String filename)
    {
        try {
            resourceStorageService.deleteByReference(filename);
            return Result.success();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Delete image by key failed: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    private boolean isSupportedImage(MultipartFile file)
    {
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }

        String extension = getFileExtension(file.getOriginalFilename());
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    private String getFileExtension(String filename)
    {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private String generateSummary(String content, int maxLength)
    {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String plainText = content
                .replaceAll("#{1,6}\\s+", "")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("\\*(.+?)\\*", "$1")
                .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1")
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                .replaceAll("`(.+?)`", "$1")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("^>\\s+", "")
                .replaceAll("^[-*+]\\s+", "")
                .replaceAll("\\n+", " ")
                .trim();

        if (plainText.length() > maxLength) {
            return plainText.substring(0, maxLength) + "...";
        }

        return plainText;
    }
}
