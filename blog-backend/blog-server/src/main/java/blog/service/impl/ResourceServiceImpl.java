package blog.service.impl;

import blog.dto.ResourceReviewDTO;
import blog.entity.SiteResource;
import blog.entity.UserAccount;
import blog.entity.UserNotification;
import blog.mapper.ResourceMapper;
import blog.mapper.UserAccountMapper;
import blog.mapper.UserNotificationMapper;
import blog.service.AccessControlService;
import blog.service.ResourceService;
import blog.service.ResourceStorageService;
import blog.vo.ResourceVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ResourceServiceImpl implements ResourceService
{
    private static final long MAX_RESOURCE_SIZE = 200L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg",
            "zip", "rar", "7z", "tar", "gz",
            "exe", "msi", "dmg", "pkg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md"
    );
    private static final Map<String, String> CATEGORY_KEY_MAP = Map.ofEntries(
            Map.entry("jpg", "image"),
            Map.entry("jpeg", "image"),
            Map.entry("png", "image"),
            Map.entry("gif", "image"),
            Map.entry("webp", "image"),
            Map.entry("bmp", "image"),
            Map.entry("svg", "image"),
            Map.entry("zip", "package"),
            Map.entry("rar", "package"),
            Map.entry("7z", "package"),
            Map.entry("tar", "package"),
            Map.entry("gz", "package"),
            Map.entry("exe", "installer"),
            Map.entry("msi", "installer"),
            Map.entry("dmg", "installer"),
            Map.entry("pkg", "installer"),
            Map.entry("pdf", "pdf"),
            Map.entry("doc", "document"),
            Map.entry("docx", "document"),
            Map.entry("xls", "document"),
            Map.entry("xlsx", "document"),
            Map.entry("ppt", "document"),
            Map.entry("pptx", "document"),
            Map.entry("txt", "document"),
            Map.entry("md", "document")
    );

    private final ResourceMapper resourceMapper;
    private final UserAccountMapper userAccountMapper;
    private final UserNotificationMapper userNotificationMapper;
    private final AccessControlService accessControlService;
    private final ResourceStorageService resourceStorageService;

    public ResourceServiceImpl(ResourceMapper resourceMapper,
                               UserAccountMapper userAccountMapper,
                               UserNotificationMapper userNotificationMapper,
                               AccessControlService accessControlService,
                               ResourceStorageService resourceStorageService)
    {
        this.resourceMapper = resourceMapper;
        this.userAccountMapper = userAccountMapper;
        this.userNotificationMapper = userNotificationMapper;
        this.accessControlService = accessControlService;
        this.resourceStorageService = resourceStorageService;
    }

    @Override
    public List<ResourceVO> listPublicResources()
    {
        return resourceMapper.selectPublicList();
    }

    @Override
    public List<ResourceVO> listMyResources(String username)
    {
        UserAccount currentUser = accessControlService.requireUser(username);
        return resourceMapper.selectByUploaderId(currentUser.getId());
    }

    @Override
    public List<ResourceVO> listAdminResources(String username, String status)
    {
        accessControlService.requireAdmin(username);
        return resourceMapper.selectAdminList(normalizeStatusFilter(status));
    }

    @Override
    public ResourceVO uploadResource(String username, MultipartFile file)
    {
        UserAccount currentUser = accessControlService.requireUser(username);
        validateUpload(file);

        String extension = extractExtension(file.getOriginalFilename());
        String categoryKey = CATEGORY_KEY_MAP.getOrDefault(extension, "generic");
        boolean admin = accessControlService.isAdmin(currentUser);
        LocalDateTime now = LocalDateTime.now();

        SiteResource resource = new SiteResource();
        resource.setUploaderId(currentUser.getId());
        resource.setOriginalName(file.getOriginalFilename().trim());
        resource.setFileUrl(resourceStorageService.uploadResource(file, "resources"));
        resource.setFileSize(file.getSize());
        resource.setExtension(extension);
        resource.setCategoryKey(categoryKey);
        resource.setMimeType(resolveMimeType(file));
        resource.setStatus(admin ? "APPROVED" : "PENDING");
        resource.setReviewNote(admin ? "管理员上传，已自动通过审核" : null);
        resource.setReviewerId(admin ? currentUser.getId() : null);
        resource.setDownloadCount(0);
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        resource.setReviewTime(admin ? now : null);
        resourceMapper.insert(resource);

        if (!admin) {
            notifyAdminsForPendingResource(currentUser, resource);
        }

        return resourceMapper.selectById(resource.getId());
    }

    @Override
    public ResourceVO reviewResource(String username, Long resourceId, ResourceReviewDTO request)
    {
        UserAccount reviewer = accessControlService.requireAdmin(username);
        if (request == null || !StringUtils.hasText(request.getStatus())) {
            throw new IllegalArgumentException("审核状态不能为空");
        }

        String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("仅支持 APPROVED 或 REJECTED 审核结果");
        }

        SiteResource existing = requireResourceEntity(resourceId);
        LocalDateTime now = LocalDateTime.now();

        SiteResource update = new SiteResource();
        update.setId(existing.getId());
        update.setStatus(status);
        update.setReviewNote(trimToNull(request.getReviewNote()));
        update.setReviewerId(reviewer.getId());
        update.setUpdateTime(now);
        update.setReviewTime(now);
        resourceMapper.updateReview(update);

        notifyUploaderForReview(existing, status, trimToNull(request.getReviewNote()));
        return resourceMapper.selectById(existing.getId());
    }

    @Override
    public void deleteResource(String username, Long resourceId)
    {
        SiteResource existing = requireResourceEntity(resourceId);
        accessControlService.requireAdminOrOwner(username, existing.getUploaderId(), "资源");
        resourceStorageService.deleteByReference(existing.getFileUrl());
        resourceMapper.deleteById(existing.getId());
    }

    @Override
    public void recordDownload(Long resourceId)
    {
        SiteResource existing = requireResourceEntity(resourceId);
        if (!"APPROVED".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("该资源尚未通过审核");
        }
        resourceMapper.incrementDownloadCount(resourceId);
    }

    private void validateUpload(MultipartFile file)
    {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的资源文件");
        }
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new IllegalArgumentException("缺少文件名称");
        }
        if (file.getSize() > MAX_RESOURCE_SIZE) {
            throw new IllegalArgumentException("资源文件大小不能超过 200MB");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("当前仅支持常见图片、压缩包、安装包和文档资源");
        }
    }

    private SiteResource requireResourceEntity(Long resourceId)
    {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }

        SiteResource resource = resourceMapper.selectEntityById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在或已删除");
        }
        return resource;
    }

    private void notifyAdminsForPendingResource(UserAccount uploader, SiteResource resource)
    {
        String uploaderName = displayName(uploader);
        for (UserAccount admin : userAccountMapper.selectAdmins()) {
            createNotification(
                    admin.getId(),
                    "RESOURCE_REVIEW_PENDING",
                    "有新的资源待审核",
                    uploaderName + " 上传了资源《" + resource.getOriginalName() + "》，请及时审核。",
                    "resource",
                    resource.getId(),
                    null
            );
        }
    }

    private void notifyUploaderForReview(SiteResource resource, String status, String reviewNote)
    {
        String title = "APPROVED".equals(status) ? "你的资源已审核通过" : "你的资源未通过审核";
        String content = "APPROVED".equals(status)
                ? "资源《" + resource.getOriginalName() + "》已通过管理员审核，现已展示到资源中心。"
                : "资源《" + resource.getOriginalName() + "》未通过审核" + (StringUtils.hasText(reviewNote) ? "，原因：" + reviewNote : "。");

        createNotification(
                resource.getUploaderId(),
                "RESOURCE_REVIEWED",
                title,
                content,
                "resource",
                resource.getId(),
                null
        );
    }

    private void createNotification(Long userId,
                                    String type,
                                    String title,
                                    String content,
                                    String targetType,
                                    Long targetId,
                                    Long relatedReportId)
    {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setRelatedReportId(relatedReportId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notification.setReadTime(null);
        userNotificationMapper.insert(notification);
    }

    private String extractExtension(String filename)
    {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveMimeType(MultipartFile file)
    {
        return StringUtils.hasText(file.getContentType()) ? file.getContentType().trim() : "application/octet-stream";
    }

    private String trimToNull(String value)
    {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String displayName(UserAccount userAccount)
    {
        if (userAccount == null) {
            return "Dream 用户";
        }
        if (StringUtils.hasText(userAccount.getNickname())) {
            return userAccount.getNickname().trim();
        }
        if (StringUtils.hasText(userAccount.getUsername())) {
            return userAccount.getUsername().trim();
        }
        return "Dream 用户";
    }

    private String normalizeStatusFilter(String status)
    {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return "ALL".equals(normalized) ? null : normalized;
    }
}
