package blog.service;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.GithubRegistrationSession;
import blog.entity.UserAccount;
<<<<<<< HEAD
import blog.vo.UserProfileVO;

=======
import blog.vo.AdminUserVO;
import blog.vo.UserProfileVO;

import java.util.List;

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

<<<<<<< HEAD
    void ensureAdminAccount();

    Long ensureDefaultAdminAndGetId();

    /**
     * 根据ID查询用户
     */
    UserAccount findById(Long userId);
=======
    List<AdminUserVO> listActiveUsers();

    AdminUserVO updateUserAdminRole(Long userId, Boolean admin, String operatorUsername);

    void deleteUser(Long userId, String operatorUsername);

    void ensureAdminAccount();

    Long ensureDefaultAdminAndGetId();
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
}
