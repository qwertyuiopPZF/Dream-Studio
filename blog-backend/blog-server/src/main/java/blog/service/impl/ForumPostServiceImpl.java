package blog.service.impl;

<<<<<<< HEAD
import blog.config.BusinessException;
import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumPostDTO;
import blog.dto.PostCreateDTO;
import blog.dto.PostQueryDTO;
import blog.entity.ForumPost;

import blog.mapper.ForumPostMapper;
import blog.result.PageResult;
import blog.service.ForumPostService;
import blog.vo.ForumPostVO;
import blog.vo.PostDetailVO;
import blog.vo.PostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
=======
import blog.dto.ForumPostAdminUpdateDTO;
import blog.dto.ForumPostDTO;
import blog.entity.ForumPost;
import blog.mapper.ForumPostMapper;
import blog.service.ForumPostService;
import blog.service.UserAccountService;
import blog.vo.ForumPostVO;
import lombok.extern.slf4j.Slf4j;
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.*;
import java.util.concurrent.CompletableFuture;
=======
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
import java.util.stream.Collectors;

@Service
@Slf4j
<<<<<<< HEAD
public class ForumPostServiceImpl implements ForumPostService {

=======
public class ForumPostServiceImpl implements ForumPostService
{
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    private static final int SUMMARY_MAX_LENGTH = 160;

    @Autowired
    private ForumPostMapper forumPostMapper;

<<<<<<< HEAD
    // ========== 原有方法 ==========

    @Override
    public Map<String, Object> listPosts(String sort, int page, int size) {
=======
    @Override
    public Map<String, Object> listPosts(String sort, int page, int size)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

<<<<<<< HEAD
        List<ForumPostVO> posts = forumPostMapper.selectPage(normalizeSort(sort), offset, safeSize, null, null, false);
        Long total = forumPostMapper.countPage(null, null, false);
=======
        List<ForumPostVO> posts = forumPostMapper.selectPage(normalizeSort(sort), offset, safeSize, null, null, null, false);
        Long total = forumPostMapper.countPage(null, null, null, false);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

        Map<String, Object> result = new HashMap<>();
        result.put("data", posts);
        result.put("pagination", buildPagination(safePage, safeSize, total));
        result.put("stats", buildPublicStats(total));
        return result;
    }

    @Override
    @Transactional
<<<<<<< HEAD
    public ForumPostVO getPostById(Long id) {
=======
    public ForumPostVO getPostById(Long id)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        ForumPostVO forumPost = forumPostMapper.selectById(id);
        if (forumPost == null) {
            return null;
        }
<<<<<<< HEAD
=======

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        forumPostMapper.incrementViewCount(id);
        return forumPostMapper.selectById(id);
    }

    @Override
<<<<<<< HEAD
    @Transactional
    public ForumPostVO createPost(ForumPostDTO forumPostDTO) {
=======
    public ForumPostVO findPostById(Long id)
    {
        return forumPostMapper.selectById(id);
    }

    @Override
    @Transactional
    public ForumPostVO createPost(ForumPostDTO forumPostDTO)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        validateForumPost(forumPostDTO);

        LocalDateTime now = LocalDateTime.now();
        ForumPost forumPost = new ForumPost();
        forumPost.setAuthorId(forumPostDTO.getAuthorId());
        forumPost.setTitle(forumPostDTO.getTitle().trim());
        forumPost.setSummary(buildSummary(forumPostDTO));
        forumPost.setContent(forumPostDTO.getContent().trim());
        forumPost.setNickname(forumPostDTO.getNickname().trim());
        forumPost.setEmail(trimToNull(forumPostDTO.getEmail()));
        forumPost.setAvatar(trimToNull(forumPostDTO.getAvatar()));
        forumPost.setViewCount(0);
        forumPost.setIsPinned(Boolean.FALSE);
        forumPost.setIsFeatured(Boolean.FALSE);
        forumPost.setCreateTime(now);
        forumPost.setUpdateTime(now);
        forumPost.setLastActivityTime(now);

        forumPostMapper.insert(forumPost);
        log.info("论坛帖子发布成功，ID：{}", forumPost.getId());
        return forumPostMapper.selectById(forumPost.getId());
    }

    @Override
<<<<<<< HEAD
    public Map<String, List<ForumPostVO>> getSidebarData(Long currentPostId, int limit) {
        int safeLimit = Math.max(limit, 1);

        List<ForumPostVO> latest = forumPostMapper.selectPage("latest", 0, safeLimit, null, currentPostId, false);
        List<ForumPostVO> recommendations = forumPostMapper.selectPage("featured", 0, safeLimit, null, currentPostId, true);

        if (recommendations.size() < safeLimit) {
            List<Long> existingIds = recommendations.stream().map(ForumPostVO::getId).collect(Collectors.toList());
            List<ForumPostVO> hotPosts = forumPostMapper.selectPage("hot", 0, safeLimit * 2, null, currentPostId, false);
=======
    public Map<String, List<ForumPostVO>> getSidebarData(Long currentPostId, int limit)
    {
        int safeLimit = Math.max(limit, 1);

        List<ForumPostVO> latest = forumPostMapper.selectPage("latest", 0, safeLimit, null, null, currentPostId, false);
        List<ForumPostVO> recommendations = forumPostMapper.selectPage("featured", 0, safeLimit, null, null, currentPostId, true);

        if (recommendations.size() < safeLimit) {
            List<Long> existingIds = recommendations.stream().map(ForumPostVO::getId).collect(Collectors.toList());
            List<ForumPostVO> hotPosts = forumPostMapper.selectPage("hot", 0, safeLimit * 2, null, null, currentPostId, false);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
    public Map<String, Object> listAdminPosts(int page, int size, String keyword) {
=======
    public Map<String, Object> listAdminPosts(int page, int size, String keyword, Long authorId)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

<<<<<<< HEAD
        List<ForumPostVO> posts = forumPostMapper.selectPage("latest", offset, safeSize, trimToNull(keyword), null, false);
        Long total = forumPostMapper.countPage(trimToNull(keyword), null, false);
=======
        List<ForumPostVO> posts = forumPostMapper.selectPage("latest", offset, safeSize, trimToNull(keyword), authorId, null, false);
        Long total = forumPostMapper.countPage(trimToNull(keyword), authorId, null, false);
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

        Map<String, Object> result = new HashMap<>();
        result.put("data", posts);
        result.put("pagination", buildPagination(safePage, safeSize, total));
        return result;
    }

    @Override
    @Transactional
<<<<<<< HEAD
    public void updateAdminPostMeta(Long id, ForumPostAdminUpdateDTO updateDTO) {
=======
    public void updateAdminPostMeta(Long id, ForumPostAdminUpdateDTO updateDTO)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        ForumPostVO existingPost = forumPostMapper.selectById(id);
        if (existingPost == null) {
            throw new IllegalArgumentException("帖子不存在");
        }

        ForumPost forumPost = new ForumPost();
        forumPost.setId(id);
        forumPost.setIsPinned(updateDTO.getIsPinned());
        forumPost.setIsFeatured(updateDTO.getIsFeatured());
        forumPostMapper.updateMeta(forumPost);
    }

    @Override
    @Transactional
<<<<<<< HEAD
    public void deletePost(Long id) {
=======
    public void deletePost(Long id)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        ForumPostVO existingPost = forumPostMapper.selectById(id);
        if (existingPost == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        forumPostMapper.deleteById(id);
    }

    @Override
<<<<<<< HEAD
    public void touchLastActivityByPage(String page) {
=======
    public void touchLastActivityByPage(String page)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        Long postId = parseForumPostId(page);
        if (postId == null) {
            return;
        }
        forumPostMapper.updateLastActivityTime(postId, LocalDateTime.now());
    }

    @Override
<<<<<<< HEAD
    public Long countTotal() {
        return forumPostMapper.countAll();
    }

    // ========== 新增方法：论坛专栏功能 ==========

    @Override
    public PageResult<PostVO> getPostList(PostQueryDTO query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        if (query.getSortBy() == null || query.getSortBy().isEmpty()) {
            query.setSortBy("time");
        }
        if (query.getOrder() == null || query.getOrder().isEmpty()) {
            query.setOrder("desc");
        }

        int offset = (query.getPage() - 1) * query.getSize();

        List<ForumPost> posts = forumPostMapper.selectPostListNew(query, offset);
        Long total = forumPostMapper.selectPostCountNew(query.getKeyword());

        List<PostVO> list = posts.stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        PageResult<PostVO> result = new PageResult<>();
        result.setPage(query.getPage());
        result.setSize(query.getSize());
        result.setTotal(total);
        result.setRecords(list);
        return result;
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        CompletableFuture.runAsync(() -> {
            try {
                forumPostMapper.incrementViewCountNew(postId);
            } catch (Exception e) {
                log.error("增加浏览量失败: {}", e.getMessage());
            }
        });

        ForumPost post = forumPostMapper.selectByIdEntity(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        post.setLastActivityTime(LocalDateTime.now());
        forumPostMapper.updateByIdEntity(post);

        return convertToPostDetailVO(post);
    }

    @Override
    @Transactional
    public Long createNewPost(PostCreateDTO dto, Long userId, String nickname, String email, String avatar) {
        String summary = dto.getSummary();
        if (!StringUtils.hasText(summary) && dto.getAutoSummary()) {
            summary = generateSummary(dto.getContent(), 200);
        }

        ForumPost post = new ForumPost();
        post.setAuthorId(userId);
        post.setTitle(dto.getTitle());
        post.setSummary(summary);
        post.setContent(dto.getContent());
        post.setNickname(nickname);
        post.setEmail(email);
        post.setAvatar(avatar);
        post.setViewCount(0);
        post.setCommentCount(0);
        post.setLikeCount(0);
        post.setIsPinned(false);
        post.setIsFeatured(false);
        post.setStatus(1);

        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        post.setLastActivityTime(now);

        int result = forumPostMapper.insertNew(post);
        if (result <= 0) {
            throw new BusinessException("发布失败");
        }

        log.info("文章发布成功: postId={}", post.getId());
        return post.getId();
    }

    @Override
    @Transactional
    public void updatePost(Long postId, PostCreateDTO dto, Long userId) {
        ForumPost post = forumPostMapper.selectByIdEntity(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        if (!post.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限修改此文章");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        if (StringUtils.hasText(dto.getSummary())) {
            post.setSummary(dto.getSummary());
        } else if (dto.getAutoSummary()) {
            post.setSummary(generateSummary(dto.getContent(), 200));
        }

        post.setUpdateTime(LocalDateTime.now());
        post.setLastActivityTime(LocalDateTime.now());

        int result = forumPostMapper.updateByIdEntity(post);
        if (result <= 0) {
            throw new BusinessException("修改失败");
        }

        log.info("文章更新成功: postId={}", postId);
    }

    @Override
    @Transactional
    public void deletePostById(Long postId, Long userId, boolean isAdmin) {
        ForumPost post = forumPostMapper.selectByIdEntity(postId);
        if (post == null) {
            throw new BusinessException("文章不存在");
        }

        if (!isAdmin && !post.getAuthorId().equals(userId)) {
            throw new BusinessException("无权限删除此文章");
        }

        int result = forumPostMapper.deleteByIdEntity(postId);
        if (result <= 0) {
            throw new BusinessException("删除失败");
        }

        log.info("文章删除成功: postId={}", postId);
    }

    @Override
    public PageResult<PostVO> getUserPostsByUserId(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<ForumPost> posts = forumPostMapper.selectByAuthorIdWithPageNew(userId, offset, size);
        Long total = forumPostMapper.countByAuthorIdNew(userId);

        List<PostVO> list = posts.stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        PageResult<PostVO> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(list);
        return result;
    }

    @Override
    public List<PostVO> getPromotedPostList() {
        List<ForumPost> posts = forumPostMapper.selectPromotedPostsNew(5);
        return posts.stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementCommentCount(Long postId) {
        forumPostMapper.incrementCommentCount(postId);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        // 仅做计数，不做去重控制
        ForumPost post = forumPostMapper.selectByIdEntity(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("文章不存在或已下线");
        }
        forumPostMapper.incrementLikeCount(postId);
    }

    @Override
    public void unlikePost(Long postId, Long userId) {
        ForumPost post = forumPostMapper.selectByIdEntity(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("文章不存在或已下线");
        }
        forumPostMapper.decrementLikeCount(postId);
    }

    // ========== 私有辅助方法 ==========

    private PostVO convertToPostVO(ForumPost post) {
        if (post == null) {
            return null;
        }
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        vo.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0);
        return vo;
    }

    private PostDetailVO convertToPostDetailVO(ForumPost post) {
        if (post == null) {
            return null;
        }
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }

    private String generateSummary(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String plainText = content
                .replaceAll("#+\\s", "")
                .replaceAll("\\*\\*|\\*", "")
                .replaceAll("\\[.*?\\]\\(.*?\\)", "")
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`[^`]*`", "")
                .replaceAll(">\\s", "")
                .replaceAll("\\n+", " ")
                .trim();

        if (plainText.length() > maxLength) {
            return plainText.substring(0, maxLength) + "...";
        }
        return plainText;
    }

    private String normalizeSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return "latest";
        }
=======
    public Long countTotal()
    {
        return forumPostMapper.countAll();
    }

    private String normalizeSort(String sort)
    {
        if (!StringUtils.hasText(sort)) {
            return "latest";
        }

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        String normalizedSort = sort.trim().toLowerCase();
        if ("hot".equals(normalizedSort) || "featured".equals(normalizedSort)) {
            return normalizedSort;
        }
        return "latest";
    }

<<<<<<< HEAD
    private void validateForumPost(ForumPostDTO forumPostDTO) {
=======
    private void validateForumPost(ForumPostDTO forumPostDTO)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
    }

<<<<<<< HEAD
    private String buildSummary(ForumPostDTO forumPostDTO) {
=======
    private String buildSummary(ForumPostDTO forumPostDTO)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

<<<<<<< HEAD
    private String trimToNull(String value) {
=======
    private String trimToNull(String value)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

<<<<<<< HEAD
    private Map<String, Object> buildPagination(int page, int size, Long total) {
=======
    private Map<String, Object> buildPagination(int page, int size, Long total)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        long safeTotal = total == null ? 0L : total;
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("totalPage", (int) Math.ceil((double) safeTotal / size));
        pagination.put("total", safeTotal);
        pagination.put("size", size);
        return pagination;
    }

<<<<<<< HEAD
    private Map<String, Object> buildPublicStats(Long total) {
=======
    private Map<String, Object> buildPublicStats(Long total)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total == null ? 0L : total);
        stats.put("featuredCount", forumPostMapper.countFeatured());
        stats.put("pinnedCount", forumPostMapper.countPinned());
        return stats;
    }

<<<<<<< HEAD
    private Long parseForumPostId(String page) {
=======
    private Long parseForumPostId(String page)
    {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
