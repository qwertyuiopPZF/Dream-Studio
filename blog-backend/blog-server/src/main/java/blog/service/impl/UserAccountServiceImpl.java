package blog.service.impl;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.entity.UserAccount;
import blog.mapper.UserAccountMapper;
import blog.service.UserAccountService;
import blog.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserAccount findByUsername(String username)
    {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userAccountMapper.selectByUsername(username);
    }

    @Override
    public UserAccount saveOrUpdateGithubUser(GithubOAuthUserDTO githubUser)
    {
        if (githubUser == null || githubUser.getGithubId() == null || !StringUtils.hasText(githubUser.getGithubLogin())) {
            throw new IllegalArgumentException("GitHub 用户信息不完整");
        }

        UserAccount existingUser = userAccountMapper.selectByGithubId(githubUser.getGithubId());
        if (existingUser == null) {
            existingUser = userAccountMapper.selectByGithubLogin(githubUser.getGithubLogin());
        }

        LocalDateTime now = LocalDateTime.now();
        if (existingUser == null) {
            UserAccount newUser = new UserAccount();
            newUser.setUsername(generateUsername(githubUser));
            newUser.setGithubId(githubUser.getGithubId());
            newUser.setGithubLogin(githubUser.getGithubLogin());
            newUser.setNickname(resolveNickname(githubUser));
            newUser.setAvatar(githubUser.getAvatar());
            newUser.setEmail(githubUser.getEmail());
            newUser.setPasswordHash(null);
            newUser.setBio(resolveBio(githubUser));
            newUser.setRole("USER");
            newUser.setStatus(true);
            newUser.setLastLoginTime(now);
            newUser.setCreateTime(now);
            newUser.setUpdateTime(now);
            userAccountMapper.insert(newUser);
            return newUser;
        }

        existingUser.setGithubId(githubUser.getGithubId());
        existingUser.setGithubLogin(githubUser.getGithubLogin());
        existingUser.setNickname(resolveNickname(githubUser));
        existingUser.setAvatar(githubUser.getAvatar());
        existingUser.setEmail(githubUser.getEmail());
        existingUser.setBio(resolveBio(githubUser));
        existingUser.setLastLoginTime(now);
        existingUser.setUpdateTime(now);
        if (!StringUtils.hasText(existingUser.getRole())) {
            existingUser.setRole("USER");
        }
        if (existingUser.getStatus() == null) {
            existingUser.setStatus(true);
        }
        userAccountMapper.updateById(existingUser);
        return existingUser;
    }

    public Long ensureDefaultAdminAndGetId()
    {
        ensureAdminAccount();
        UserAccount admin = userAccountMapper.selectByUsername("admin");
        return admin != null ? admin.getId() : null;
    }

    @Override
    public UserProfileVO getProfileByUsername(String username)
    {
        UserAccount userAccount = findByUsername(username);
        if (userAccount != null) {
            return new UserProfileVO(
                    userAccount.getId(),
                    userAccount.getUsername(),
                    userAccount.getNickname(),
                    userAccount.getAvatar(),
                    userAccount.getEmail(),
                    userAccount.getBio(),
                    userAccount.getRole(),
                    userAccount.getGithubLogin(),
                    StringUtils.hasText(userAccount.getPasswordHash())
            );
        }

        if ("admin".equalsIgnoreCase(username)) {
            return new UserProfileVO(
                    null,
                    "admin",
                    "管理员",
                    "/avatar.png",
                    "",
                    "管理员账户 · 可在用户端执行管理操作",
                    "ADMIN",
                    null,
                    true
            );
        }

        return null;
    }

    @Override
    public void updatePassword(String username, String newPassword)
    {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }

        UserAccount userAccount = userAccountMapper.selectByUsername(username);
        if (userAccount == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccount.setUpdateTime(LocalDateTime.now());
        userAccountMapper.updateById(userAccount);
    }

    @Override
    public void ensureAdminAccount()
    {
        UserAccount existingAdmin = userAccountMapper.selectByUsername("admin");
        LocalDateTime now = LocalDateTime.now();
        if (existingAdmin == null) {
            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setGithubId(null);
            admin.setGithubLogin(null);
            admin.setNickname("管理员");
            admin.setAvatar("/avatar.png");
            admin.setEmail("");
            admin.setPasswordHash(passwordEncoder.encode("123456"));
            admin.setBio("管理员账户 · 用于站点管理测试");
            admin.setRole("ADMIN");
            admin.setStatus(true);
            admin.setLastLoginTime(now);
            admin.setCreateTime(now);
            admin.setUpdateTime(now);
            userAccountMapper.insert(admin);
            return;
        }

        boolean changed = false;
        if (!StringUtils.hasText(existingAdmin.getRole()) || !"ADMIN".equalsIgnoreCase(existingAdmin.getRole())) {
            existingAdmin.setRole("ADMIN");
            changed = true;
        }
        if (!StringUtils.hasText(existingAdmin.getPasswordHash())) {
            existingAdmin.setPasswordHash(passwordEncoder.encode("123456"));
            changed = true;
        }
        if (!StringUtils.hasText(existingAdmin.getNickname())) {
            existingAdmin.setNickname("管理员");
            changed = true;
        }
        if (existingAdmin.getStatus() == null) {
            existingAdmin.setStatus(true);
            changed = true;
        }
        if (changed) {
            existingAdmin.setUpdateTime(now);
            userAccountMapper.updateById(existingAdmin);
        }
    }

    private String generateUsername(GithubOAuthUserDTO githubUser)
    {
        String sanitizedLogin = githubUser.getGithubLogin().replaceAll("[^A-Za-z0-9_-]", "_");
        String username = "github_" + sanitizedLogin;
        UserAccount sameUsernameUser = userAccountMapper.selectByUsername(username);
        if (sameUsernameUser == null) {
            return username;
        }
        return username + "_" + githubUser.getGithubId();
    }

    private String resolveNickname(GithubOAuthUserDTO githubUser)
    {
        if (StringUtils.hasText(githubUser.getNickname())) {
            return githubUser.getNickname();
        }
        return githubUser.getGithubLogin();
    }

    private String resolveBio(GithubOAuthUserDTO githubUser)
    {
        if (StringUtils.hasText(githubUser.getBio())) {
            return githubUser.getBio();
        }
        return githubUser.getGithubLogin() + " 的 GitHub 主页";
    }
}
