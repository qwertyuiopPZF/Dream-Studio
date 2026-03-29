package blog.controller.admin;

import blog.dto.ArticleDTO;
import blog.dto.ArticleQueryDTO;
import blog.entity.UserAccount;
import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.ArticleService;
import blog.vo.ArticleVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("adminArticleController")
@RequestMapping("/admin/articles")
@Slf4j
@ApiOperation("文章管理页面")
public class ArticleController
{
    @Autowired
    private ArticleService articleService;

    @Autowired
    private AccessControlService accessControlService;

    @PostMapping
    public Result<String> saveArticle(@RequestBody ArticleDTO articleDTO,
                                      Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        log.info("保存文章：{}", articleDTO);
        try {
            validatePublishPayload(articleDTO);
            UserAccount currentUser = accessControlService.requireUser(authentication.getName());
            sanitizeFeaturedPayload(articleDTO, currentUser);
            articleDTO.setAuthorId(currentUser.getId());
            articleService.saveArticle(articleDTO);

            String message = articleDTO.getStatus() != null && articleDTO.getStatus() == 1 ? "文章发布成功" : "草稿保存成功";
            return Result.success(message);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<String> updateArticle(@PathVariable Long id,
                                        @RequestBody ArticleDTO articleDTO,
                                        Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        log.info("更新文章，ID：{}，数据：{}", id, articleDTO);
        try {
            validatePublishPayload(articleDTO);
            ArticleVO existingArticle = articleService.findArticleById(id);
            if (existingArticle == null) {
                return Result.error("文章不存在");
            }

            UserAccount currentUser = accessControlService.requireAdminOrOwner(authentication.getName(), existingArticle.getAuthorId(), "该文章");
            sanitizeFeaturedPayload(articleDTO, currentUser);
            articleDTO.setAuthorId(existingArticle.getAuthorId());
            articleService.updateArticle(id, articleDTO);

            String message = articleDTO.getStatus() != null && articleDTO.getStatus() == 1 ? "文章更新成功" : "草稿保存成功";
            return Result.success(message);
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("获取文章列表")
    public Result<Map<String, Object>> getArticlesList(@RequestParam(required = false) Long categoryId,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        log.info("获取文章列表，分类ID：{}，分类名称：{}，关键词：{}，页码：{}，每页数量：{}", categoryId, category, keyword, page, size);

        try {
            UserAccount currentUser = accessControlService.requireUser(authentication.getName());
            ArticleQueryDTO queryDTO = new ArticleQueryDTO();
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryDTO.setKeyword(keyword);
            }
            if (!accessControlService.isAdmin(currentUser)) {
                queryDTO.setAuthorId(currentUser.getId());
            }

            List<ArticleVO> allArticles = articleService.listArticles(queryDTO);
            if (allArticles == null) {
                allArticles = new ArrayList<>();
            }

            if (categoryId != null) {
                allArticles = allArticles.stream()
                        .filter(article -> article.getCategoryId() != null && article.getCategoryId().equals(categoryId))
                        .collect(Collectors.toList());
            }
            if (category != null && !category.trim().isEmpty()) {
                allArticles = allArticles.stream()
                        .filter(article -> article.getCategoryName() != null && article.getCategoryName().equals(category))
                        .collect(Collectors.toList());
            }

            int total = allArticles.size();
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);
            List<ArticleVO> articles = startIndex < total ? allArticles.subList(startIndex, endIndex) : new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            result.put("data", articles);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPage", (int) Math.ceil((double) total / size));
            pagination.put("total", total);
            pagination.put("size", size);
            result.put("pagination", pagination);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("data", new ArrayList<>());

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("totalPage", 0);
            pagination.put("total", 0);
            pagination.put("size", size);
            result.put("pagination", pagination);
            return Result.success(result);
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取文章详情")
    public Result<ArticleVO> getArticleById(@PathVariable Long id,
                                            Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        ArticleVO article = articleService.findArticleById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), article.getAuthorId(), "该文章");
            return Result.success(article);
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id,
                                      Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "请先登录");
        }

        ArticleVO article = articleService.findArticleById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }

        try {
            accessControlService.requireAdminOrOwner(authentication.getName(), article.getAuthorId(), "该文章");
            articleService.deleteArticle(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    private void validatePublishPayload(ArticleDTO articleDTO)
    {
        if (articleDTO == null) {
            throw new IllegalArgumentException("文章内容不能为空");
        }

        if (articleDTO.getStatus() != null && articleDTO.getStatus() == 1) {
            if (articleDTO.getContent() == null || articleDTO.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("发布文章时内容不能为空");
            }
            if (articleDTO.getCategoryId() == null) {
                throw new IllegalArgumentException("发布文章时必须选择分类");
            }
            if (articleDTO.getTags() == null) {
                throw new IllegalArgumentException("请选择标签");
            }
        }
    }

    private void sanitizeFeaturedPayload(ArticleDTO articleDTO, UserAccount currentUser)
    {
        if (articleDTO == null || accessControlService.isAdmin(currentUser)) {
            return;
        }

        articleDTO.setIsFeatured(null);
    }
}
