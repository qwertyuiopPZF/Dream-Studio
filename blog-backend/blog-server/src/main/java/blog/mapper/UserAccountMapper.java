package blog.mapper;

import blog.entity.UserAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAccountMapper
{
    @Select("SELECT * FROM user_account WHERE id = #{id} LIMIT 1")
    UserAccount selectById(Long id);

    @Select("SELECT * FROM user_account WHERE username = #{username} LIMIT 1")
    UserAccount selectByUsername(String username);

    @Select("SELECT * FROM user_account WHERE github_id = #{githubId} LIMIT 1")
    UserAccount selectByGithubId(Long githubId);

    @Select("SELECT * FROM user_account WHERE github_login = #{githubLogin} LIMIT 1")
    UserAccount selectByGithubLogin(String githubLogin);

    @Insert("INSERT INTO user_account(username, github_id, github_login, nickname, avatar, email, password_hash, bio, role, status, last_login_time, create_time, update_time) " +
            "VALUES(#{username}, #{githubId}, #{githubLogin}, #{nickname}, #{avatar}, #{email}, #{passwordHash}, #{bio}, #{role}, #{status}, #{lastLoginTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserAccount userAccount);

    @Update("UPDATE user_account SET github_id = #{githubId}, github_login = #{githubLogin}, nickname = #{nickname}, avatar = #{avatar}, email = #{email}, password_hash = #{passwordHash}, bio = #{bio}, role = #{role}, status = #{status}, last_login_time = #{lastLoginTime}, update_time = #{updateTime} WHERE id = #{id}")
    void updateById(UserAccount userAccount);
}
