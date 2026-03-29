package blog.service.impl;

import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumPostDTO;
import blog.entity.ForumPost;
import blog.entity.Tags;
import blog.mapper.CategoryMapper;
import blog.mapper.ForumPostMapper;
import blog.mapper.TagsMapper;
import blog.service.ForumPostService;
import blog.service.UserAccountService;
import blog.vo.ForumPostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ForumPostServiceImpl implements ForumPostService
{
    private static final int SUMMARY_MAX_LENGTH = 160;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagsMapper tagsMapper;

    @Override
    public Map<String, Object> listPosts(String sort, int page, int size)
    {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ForumPostVO> posts = forumPostMapper.selectPage(normalizeSort(sort), offset, safeSize, null, null, null, false, 1);
        attachTagNames(posts);
        Long total = forumPostMapper.countPage(null, null, null, false, 1);

        Map<String, Object> result = new HashMap<>();
        result.put("data", posts);
        result.put("pagination", buildPagination(safePage, safeSize, total));
        result.put("stats", buildPublicStats(total));
        return result;
    }

    @Override
    @Transactional
    public ForumPostVO getPostById(Long id)
    {
        ForumPostVO forumPost = forumPostMapper.selectById(id, 1);
        if (forumPost == null) {
            return null;
        }

        forumPostMapper.incrementViewCount(id);
        ForumPostVO latestPost = forumPostMapper.selectById(id, 1);
        attachTagNames(latestPost);
        return latestPost;
    }

    @Override
    public ForumPostVO findPostById(Long id)
    {
        ForumPostVO forumPost = forumPostMapper.selectById(id, null);
        attachTagNames(forumPost);
        return forumPost;
    }

    @Override
    @Transactional
    public ForumPostVO createPost(ForumPostDTO forumPostDTO)
    {
        validateForumPost(forumPostDTO);

        LocalDateTime now = LocalDateTime.now();
        ForumPost forumPost = new ForumPost();
        forumPost.setAuthorId(forumPostDTO.getAuthorId());
        forumPost.setTitle(forumPostDTO.getTitle().trim());
        forumPost.setSummary(buildSummary(forumPostDTO));
        forumPost.setContent(forumPostDTO.getContent().trim());
        forumPost.setCategoryId(forumPostDTO.getCategoryId());
        forumPost.setTags(normalizeTagIds(forumPostDTO.getTags()));
        forumPost.setNickname(forumPostDTO.getNickname().trim());
        forumPost.setEmail(trimToNull(forumPostDTO.getEmail()));
        forumPost.setAvatar(trimToNull(forumPostDTO.getAvatar()));
        forumPost.setViewCount(0);
        forumPost.setStatus(1);
        forumPost.setIsPinned(Boolean.FALSE);
        forumPost.setIsFeatured(Boolean.FALSE);
        forumPost.setCreateTime(now);
        forumPost.setUpdateTime(now);
        forumPost.setLastActivityTime(now);

        forumPostMapper.insert(forumPost);
        log.info("论坛帖子发布成功，ID：{}", forumPost.getId());
        ForumPostVO createdPost = forumPostMapper.selectById(forumPost.getId(), null);
        attachTagNames(createdPost);
        return createdPost;
    }

    @Override
    public Map<String, List<ForumPostVO>> getSidebarData(Long currentPostId, int limit)
    {
        int safeLimit = Math.max(limit, 1);

        List<ForumPostVO> latest = forumPostMapper.selectPage("latest", 0, safeLimit, null, null, currentPostId, false, 1);
        List<ForumPostVO> recommendations = forumPostMapper.selectPage("featured", 0, safeLimit, null, null, currentPostId, true, 1);
        attachTagNames(latest);
        attachTagNames(recommendations);

        if (recommendations.size() < safeLimit) {
            List<Long> existingIds = recommendations.stream().map(ForumPostVO::getId).collect(Collectors.toList());
            List<ForumPostVO> hotPosts = forumPostMapper.selectPage("hot", 0, safeLimit * 2, null, null, currentPostId, false, 1);
            attachTagNames(hotPosts);
            for (ForumPostVO hotPost : hotPosts) {
                if (recommendations.size() >= safeLimit) {
                    break;
                }
                if (!existingIds.contains(hotPost.getId())) {
                    recommendations.add(hotPost);
                    existingIds.add(hotPost.getId());
                }
            }
        }

        Map<String, List<ForumPostVO>> result = new HashMap<>();
        result.put("latest", latest);
        result.put("recommendations", recommendations);
        return result;
    }

    @Override
    public Map<String, Object> listAdminPosts(int page, int size, String keyword, Long authorId)
    {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<ForumPostVO> posts = forumPostMapper.selectPage("latest", offset, safeSize, trimToNull(keyword), authorId, null, false, null);
        attachTagNames(posts);
        Long total = forumPostMapper.countPage(trimToNull(keyword), authorId, null, false, null);

        Map<String, Object> result = new HashMap<>();
        result.put("data", posts);
        result.put("pagination", buildPagination(safePage, safeSize, total));
        return result;
    }

    @Override
    @Transactional
    public void updateAdminPostMeta(Long id, ForumPostAdminUpdateDTO updateDTO)
    {
        ForumPostVO existingPost = forumPostMapper.selectById(id, null);
        if (existingPost == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (updateDTO == null) {
            throw new IllegalArgumentException("更新内容不能为空");
        }

        ForumPost forumPost = new ForumPost();
        forumPost.setId(id);
        forumPost.setIsPinned(Boolean.TRUE.equals(updateDTO.getIsPinned()));
        forumPost.setIsFeatured(Boolean.TRUE.equals(updateDTO.getIsFeatured()));
        forumPost.setCategoryId(validateAdminCategory(updateDTO.getCategoryId()));
        forumPost.setTags(normalizeTagIds(updateDTO.getTags()));
        forumPostMapper.updateMeta(forumPost);
    }

    @Override
    @Transactional
    public void updatePostStatus(Long id, Integer status)
    {
        ForumPostVO existingPost = forumPostMapper.selectById(id, null);
        if (existingPost == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("帖子状态无效");
        }

        forumPostMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void deletePost(Long id)
    {
        ForumPostVO existingPost = forumPostMapper.selectById(id, null);
        if (existingPost == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        forumPostMapper.deleteById(id);
    }

    @Override
    public void touchLastActivityByPage(String page)
    {
        Long postId = parseForumPostId(page);
        if (postId == null) {
            return;
        }
        forumPostMapper.updateLastActivityTime(postId, LocalDateTime.now());
    }

    @Override
    public Long countTotal()
    {
        return forumPostMapper.countAll();
    }

    private String normalizeSort(String sort)
    {
        if (!StringUtils.hasText(sort)) {
            return "latest";
        }

        String normalizedSort = sort.trim().toLowerCase();
        if ("hot".equals(normalizedSort) || "featured".equals(normalizedSort)) {
            return normalizedSort;
        }
        return "latest";
    }

    private void validateForumPost(ForumPostDTO forumPostDTO)
    {
        if (forumPostDTO == null) {
            throw new IllegalArgumentException("帖子内容不能为空");
        }
        if (!StringUtils.hasText(forumPostDTO.getNickname())) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (!StringUtils.hasText(forumPostDTO.getTitle())) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (!StringUtils.hasText(forumPostDTO.getContent())) {
            throw new IllegalArgumentException("正文不能为空");
        }
        if (forumPostDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("请选择分类");
        }
        if (categoryMapper.countById(forumPostDTO.getCategoryId()) <= 0) {
            throw new IllegalArgumentException("所选分类不存在");
        }

        if (forumPostDTO.getNickname().trim().length() > 50) {
            throw new IllegalArgumentException("昵称不能超过50字符");
        }
        if (forumPostDTO.getTitle().trim().length() > 120) {
            throw new IllegalArgumentException("标题不能超过120字符");
        }
        if (StringUtils.hasText(forumPostDTO.getSummary()) && forumPostDTO.getSummary().trim().length() > 300) {
            throw new IllegalArgumentException("摘要不能超过300字符");
        }
        if (forumPostDTO.getContent().trim().length() > 20000) {
            throw new IllegalArgumentException("正文不能超过20000字符");
        }

        normalizeTagIds(forumPostDTO.getTags());
    }

    private String buildSummary(ForumPostDTO forumPostDTO)
    {
        if (StringUtils.hasText(forumPostDTO.getSummary())) {
            return forumPostDTO.getSummary().trim();
        }

        String content = forumPostDTO.getContent().trim();
        String plainText = content
                .replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("`([^`]*)`", "$1")
                .replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", " ")
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1")
                .replaceAll("[#>*_~|-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (!StringUtils.hasText(plainText)) {
            return "暂无摘要";
        }

        if (plainText.length() <= SUMMARY_MAX_LENGTH) {
            return plainText;
        }
        return plainText.substring(0, SUMMARY_MAX_LENGTH) + "...";
    }

    private String trimToNull(String value)
    {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Long validateAdminCategory(Long categoryId)
    {
        if (categoryId == null) {
            return null;
        }
        if (categoryMapper.countById(categoryId) <= 0) {
            throw new IllegalArgumentException("所选分类不存在");
        }
        return categoryId;
    }

    private String normalizeTagIds(String rawTags)
    {
        if (!StringUtils.hasText(rawTags)) {
            return null;
        }

        Set<Long> tagIds = Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::parseTagId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (tagIds.isEmpty()) {
            return null;
        }

        List<Long> normalizedTagIds = new ArrayList<>(tagIds);
        List<Tags> existingTags = tagsMapper.selectByIds(normalizedTagIds);
        if (existingTags.size() != normalizedTagIds.size()) {
            throw new IllegalArgumentException("所选标签不存在");
        }

        return normalizedTagIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private Long parseTagId(String value)
    {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("标签参数无效");
        }
    }

    private void attachTagNames(ForumPostVO post)
    {
        if (post == null) {
            return;
        }
        List<ForumPostVO> singlePost = new ArrayList<>();
        singlePost.add(post);
        attachTagNames(singlePost);
    }

    private void attachTagNames(List<ForumPostVO> posts)
    {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        Set<Long> tagIds = posts.stream()
                .map(ForumPostVO::getTagIds)
                .filter(StringUtils::hasText)
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::parseTagId)
                .collect(Collectors.toSet());

        if (tagIds.isEmpty()) {
            posts.forEach(post -> post.setTags(null));
            return;
        }

        Map<Long, String> tagNameMap = tagsMapper.selectByIds(new ArrayList<>(tagIds)).stream()
                .collect(Collectors.toMap(Tags::getId, Tags::getName));

        posts.forEach(post -> {
            if (!StringUtils.hasText(post.getTagIds())) {
                post.setTags(null);
                return;
            }

            String resolvedTags = Arrays.stream(post.getTagIds().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(this::parseTagId)
                    .map(tagNameMap::get)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(","));
            post.setTags(StringUtils.hasText(resolvedTags) ? resolvedTags : null);
        });
    }

    private Map<String, Object> buildPagination(int page, int size, Long total)
    {
        long safeTotal = total == null ? 0L : total;
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPage", (int) Math.ceil((double) safeTotal / size));
        pagination.put("total", safeTotal);
        pagination.put("size", size);
        return pagination;
    }

    private Map<String, Object> buildPublicStats(Long total)
    {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total == null ? 0L : total);
        stats.put("featuredCount", forumPostMapper.countFeatured());
        stats.put("pinnedCount", forumPostMapper.countPinned());
        return stats;
    }

    private Long parseForumPostId(String page)
    {
        if (!StringUtils.hasText(page) || !page.startsWith("forum-post-")) {
            return null;
        }
        try {
            return Long.parseLong(page.substring("forum-post-".length()));
        } catch (NumberFormatException e) {
            log.warn("无法从页面标识解析论坛帖子ID：{}", page);
            return null;
        }
    }
}
