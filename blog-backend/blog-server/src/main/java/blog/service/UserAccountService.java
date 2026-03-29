package blog.service;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.GithubRegistrationSession;
import blog.entity.UserAccount;
import blog.vo.AdminUserVO;
import blog.vo.UserProfileVO;

import java.util.List;

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

    List<AdminUserVO> listActiveUsers();

    AdminUserVO updateUserAdminRole(Long userId, Boolean admin, String operatorUsername);

    void deleteUser(Long userId, String operatorUsername);

    void ensureAdminAccount();

    Long ensureDefaultAdminAndGetId();
}
