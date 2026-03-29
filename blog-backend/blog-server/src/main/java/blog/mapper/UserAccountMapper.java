package blog.mapper;

import blog.entity.UserAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    @Select("SELECT * FROM user_account WHERE role = 'ADMIN' AND status = TRUE")
    List<UserAccount> selectAdmins();

    @Select("SELECT * FROM user_account WHERE status = TRUE ORDER BY CASE WHEN role = 'ADMIN' THEN 0 ELSE 1 END, COALESCE(last_login_time, create_time) DESC")
    List<UserAccount> selectActiveUsers();

    @Select("SELECT COUNT(*) FROM user_account WHERE role = 'ADMIN' AND status = TRUE")
    Long countActiveAdmins();

    @Insert("INSERT INTO user_account(username, github_id, github_login, nickname, avatar, email, phone, password_hash, bio, role, status, last_login_time, create_time, update_time) " +
            "VALUES(#{username}, #{githubId}, #{githubLogin}, #{nickname}, #{avatar}, #{email}, #{phone}, #{passwordHash}, #{bio}, #{role}, #{status}, #{lastLoginTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserAccount userAccount);

    @Update("UPDATE user_account SET username = #{username}, github_id = #{githubId}, github_login = #{githubLogin}, nickname = #{nickname}, avatar = #{avatar}, email = #{email}, phone = #{phone}, password_hash = #{passwordHash}, bio = #{bio}, role = #{role}, status = #{status}, last_login_time = #{lastLoginTime}, update_time = #{updateTime} WHERE id = #{id}")
    void updateById(UserAccount userAccount);
}
