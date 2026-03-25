package blog.service;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.GithubRegistrationSession;
import blog.entity.UserAccount;
import blog.vo.UserProfileVO;

public interface UserAccountService
{
    UserAccount findByUsername(String username);

    UserAccount saveOrUpdateGithubUser(GithubOAuthUserDTO githubUser);

    UserAccount findByGithubIdentity(Long githubId, String githubLogin);

    UserAccount completeGithubRegistration(GithubRegistrationSession registrationSession,
                                           String username,
                                           String phone,
                                           String password);

    UserProfileVO getProfileByUsername(String username);

    void updatePassword(String username, String newPassword);

    UserProfileVO updateAvatar(String username, String avatar);

    void ensureAdminAccount();

    Long ensureDefaultAdminAndGetId();

    /**
     * 根据ID查询用户
     */
    UserAccount findById(Long userId);
}
