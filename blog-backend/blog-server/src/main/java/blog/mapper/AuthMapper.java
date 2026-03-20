package blog.mapper;


import blog.entity.Auth;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface AuthMapper {

    /**
     * 根据 githubId 查询用户（判断是否存在）
     */
    Auth selectByGithubId(@Param("githubId") String githubId);

    /**
     * 根据 username 查询用户（可选，后续账号密码登录用）
     */
    Auth selectByUsername(@Param("username") String username);

    /**
     * 插入新用户（首次GitHub登录）
     */
    int insertAuth(Auth auth);

    /**
     * 更新用户信息（完善手机号/密码/昵称等）
     */
    int updateAuth(Auth auth);

    /**
     * 更新最后登录时间和IP（每次登录成功调用）
     */
    int updateLastLoginInfo(@Param("id") Long id,
                            @Param("lastLoginTime") LocalDateTime lastLoginTime,
                            @Param("lastLoginIp") String lastLoginIp);
}
