package blog.config;

import blog.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class UserAccountInitializer implements CommandLineRunner
{
    private final UserAccountService userAccountService;
    private final JdbcTemplate jdbcTemplate;

    public UserAccountInitializer(UserAccountService userAccountService,
                                  JdbcTemplate jdbcTemplate)
    {
        this.userAccountService = userAccountService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args)
    {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS user_account (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                      username VARCHAR(100) NOT NULL COMMENT '系统用户名',
                      github_id BIGINT DEFAULT NULL COMMENT 'GitHub 用户ID',
                      github_login VARCHAR(100) DEFAULT NULL COMMENT 'GitHub 登录名',
                      nickname VARCHAR(100) NOT NULL COMMENT '昵称',
                      avatar VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
                      email VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
                      password_hash VARCHAR(255) DEFAULT NULL COMMENT '站内密码哈希',
                      bio VARCHAR(500) DEFAULT NULL COMMENT '简介',
                      role VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT '角色',
                      status TINYINT(1) NOT NULL DEFAULT '1' COMMENT '状态',
                      last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_user_account_username (username),
                      UNIQUE KEY uk_user_account_github_id (github_id),
                      UNIQUE KEY uk_user_account_github_login (github_login)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点用户账号表（GitHub OAuth）'
                    """);

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS forum_post (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
                      author_id BIGINT DEFAULT NULL COMMENT '作者用户ID',
                      title VARCHAR(200) NOT NULL COMMENT '帖子标题',
                      summary VARCHAR(500) DEFAULT NULL COMMENT '帖子摘要',
                      content LONGTEXT NOT NULL COMMENT '帖子正文（Markdown）',
                      nickname VARCHAR(100) NOT NULL COMMENT '发帖昵称',
                      email VARCHAR(200) DEFAULT NULL COMMENT '邮箱',
                      avatar VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
                      view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
                      is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
                      is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加精',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      last_activity_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
                      PRIMARY KEY (id),
                      KEY idx_forum_post_activity (last_activity_time),
                      KEY idx_forum_post_featured (is_featured, is_pinned),
                      KEY idx_forum_post_create_time (create_time)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表'
                    """);

            ensureColumnExists("article", "author_id", "ALTER TABLE article ADD COLUMN author_id BIGINT DEFAULT NULL COMMENT '作者用户ID' AFTER id");
            ensureColumnExists("forum_post", "author_id", "ALTER TABLE forum_post ADD COLUMN author_id BIGINT DEFAULT NULL COMMENT '作者用户ID' AFTER id");
            ensureColumnExists("comment", "user_id", "ALTER TABLE comment ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '评论用户ID' AFTER id");

            userAccountService.ensureAdminAccount();
            Long adminId = userAccountService.ensureDefaultAdminAndGetId();
            if (adminId != null) {
                jdbcTemplate.update("UPDATE article SET author_id = ? WHERE author_id IS NULL", adminId);
                jdbcTemplate.update("UPDATE forum_post SET author_id = ? WHERE author_id IS NULL", adminId);
            }
            log.info("已确保 admin 测试账号存在");
        } catch (Exception e) {
            log.warn("初始化 admin 测试账号失败：{}", e.getMessage());
        }
    }

    private void ensureColumnExists(String tableName, String columnName, String alterSql)
    {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );

        if (Objects.equals(count, 0)) {
            jdbcTemplate.execute(alterSql);
        }
    }
}
