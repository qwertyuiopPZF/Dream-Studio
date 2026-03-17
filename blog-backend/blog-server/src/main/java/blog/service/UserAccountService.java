package blog.service;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.entity.UserAccount;
import blog.vo.UserProfileVO;

public interface UserAccountService
{
    UserAccount findByUsername(String username);

    UserAccount saveOrUpdateGithubUser(GithubOAuthUserDTO githubUser);

    UserProfileVO getProfileByUsername(String username);

    void updatePassword(String username, String newPassword);

    void ensureAdminAccount();

    Long ensureDefaultAdminAndGetId();
}
