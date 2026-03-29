package blog.service.impl;

import blog.dto.Login.GithubOAuthUserDTO;
import blog.dto.Login.GithubRegistrationSession;
import blog.entity.UserAccount;
import blog.mapper.UserAccountMapper;
import blog.service.UserAccountService;
import blog.vo.AdminUserVO;
import blog.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public UserAccount findByGithubIdentity(Long githubId, String githubLogin)
    {
        UserAccount existingUser = null;
        if (githubId != null) {
            existingUser = userAccountMapper.selectByGithubId(githubId);
        }
        if (existingUser == null && StringUtils.hasText(githubLogin)) {
            existingUser = userAccountMapper.selectByGithubLogin(githubLogin);
        }
        return existingUser;
    }

    @Override
    public UserAccount completeGithubRegistration(GithubRegistrationSession registrationSession,
                                                  String username,
                                                  String phone,
                                                  String password)
    {
        if (registrationSession == null || registrationSession.getGithubId() == null || !StringUtils.hasText(registrationSession.getGithubLogin())) {
            throw new IllegalArgumentException("GitHub 注册会话已失效，请重新发起授权");
        }

        String normalizedUsername = normalizeUsername(username);
        String normalizedPhone = normalizePhone(phone);
        validatePassword(password);

        UserAccount currentOwner = userAccountMapper.selectByUsername(normalizedUsername);
        if (currentOwner != null && !currentOwner.getId().equals(registrationSession.getExistingUserId())) {
            throw new IllegalArgumentException("用户名已存在，请更换后重试");
        }

        UserAccount userAccount = registrationSession.getExistingUserId() != null
                ? userAccountMapper.selectById(registrationSession.getExistingUserId())
                : null;

        if (userAccount == null) {
            userAccount = new UserAccount();
            userAccount.setCreateTime(LocalDateTime.now());
            userAccount.setRole("USER");
            userAccount.setStatus(true);
        }

        LocalDateTime now = LocalDateTime.now();
        userAccount.setUsername(normalizedUsername);
        userAccount.setGithubId(registrationSession.getGithubId());
        userAccount.setGithubLogin(registrationSession.getGithubLogin());
        userAccount.setNickname(resolveNickname(registrationSession));
        userAccount.setAvatar(registrationSession.getAvatar());
        userAccount.setEmail(registrationSession.getEmail());
        userAccount.setPhone(normalizedPhone);
        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setBio(resolveBio(registrationSession));
        userAccount.setLastLoginTime(now);
        userAccount.setUpdateTime(now);

        if (userAccount.getId() == null) {
            userAccountMapper.insert(userAccount);
        } else {
            userAccountMapper.updateById(userAccount);
        }

        return userAccount;
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
        if (userAccount != null && !Boolean.FALSE.equals(userAccount.getStatus())) {
            return new UserProfileVO(
                    userAccount.getId(),
                    userAccount.getUsername(),
                    userAccount.getNickname(),
                    userAccount.getAvatar(),
                    userAccount.getEmail(),
                    userAccount.getPhone(),
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
    public UserProfileVO updateAvatar(String username, String avatar)
    {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        String normalizedAvatar = avatar == null ? "" : avatar.trim();
        if (!StringUtils.hasText(normalizedAvatar)) {
            throw new IllegalArgumentException("头像地址不能为空");
        }
        if (normalizedAvatar.length() > 500) {
            throw new IllegalArgumentException("头像地址长度不能超过 500 个字符");
        }
        if (!(normalizedAvatar.startsWith("http://")
                || normalizedAvatar.startsWith("https://")
                || normalizedAvatar.startsWith("/"))) {
            throw new IllegalArgumentException("头像地址仅支持 http(s) 或站内相对路径");
        }

        UserAccount userAccount = userAccountMapper.selectByUsername(username);
        if (userAccount == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        userAccount.setAvatar(normalizedAvatar);
        userAccount.setUpdateTime(LocalDateTime.now());
        userAccountMapper.updateById(userAccount);
        return getProfileByUsername(username);
    }

    @Override
    public List<AdminUserVO> listActiveUsers()
    {
        return userAccountMapper.selectActiveUsers().stream()
                .map(this::toAdminUserVO)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserVO updateUserAdminRole(Long userId, Boolean admin, String operatorUsername)
    {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        UserAccount operator = requireActiveUser(operatorUsername);
        if (!isAdmin(operator)) {
            throw new IllegalArgumentException("当前用户没有管理员权限");
        }

        UserAccount targetUser = requireActiveUser(userId);
        if (operator.getId() != null && operator.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("不能修改自己的管理员状态");
        }

        boolean makeAdmin = Boolean.TRUE.equals(admin);
        if (!makeAdmin && isAdmin(targetUser) && safeAdminCount() <= 1) {
            throw new IllegalArgumentException("至少需要保留一名管理员");
        }

        targetUser.setRole(makeAdmin ? "ADMIN" : "USER");
        targetUser.setUpdateTime(LocalDateTime.now());
        userAccountMapper.updateById(targetUser);
        return toAdminUserVO(targetUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String operatorUsername)
    {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        UserAccount operator = requireActiveUser(operatorUsername);
        if (!isAdmin(operator)) {
            throw new IllegalArgumentException("当前用户没有管理员权限");
        }

        UserAccount targetUser = requireActiveUser(userId);
        if (operator.getId() != null && operator.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("不能删除当前登录账号");
        }

        if (isAdmin(targetUser) && safeAdminCount() <= 1) {
            throw new IllegalArgumentException("至少需要保留一名管理员");
        }

        LocalDateTime now = LocalDateTime.now();
        targetUser.setUsername(buildDeletedUsername(targetUser.getId()));
        targetUser.setGithubId(null);
        targetUser.setGithubLogin(null);
        targetUser.setNickname("已注销用户");
        targetUser.setAvatar("/avatar.png");
        targetUser.setEmail("");
        targetUser.setPhone("");
        targetUser.setPasswordHash(null);
        targetUser.setBio("账号已删除");
        targetUser.setRole("USER");
        targetUser.setStatus(false);
        targetUser.setLastLoginTime(null);
        targetUser.setUpdateTime(now);
        userAccountMapper.updateById(targetUser);
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
            admin.setPhone("");
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

    private String normalizeUsername(String username)
    {
        String normalized = username == null ? "" : username.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("请输入用户名");
        }
        if (normalized.length() < 3 || normalized.length() > 30) {
            throw new IllegalArgumentException("用户名长度需为 3 到 30 位");
        }
        if (!normalized.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("用户名仅支持字母、数字、下划线和中划线");
        }
        return normalized;
    }

    private String normalizePhone(String phone)
    {
        String normalized = phone == null ? "" : phone.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("请输入手机号");
        }
        if (!normalized.matches("^[0-9+\\-]{6,20}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        return normalized;
    }

    private void validatePassword(String password)
    {
        if (!StringUtils.hasText(password) || password.trim().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }
    }

    private String resolveNickname(GithubOAuthUserDTO githubUser)
    {
        if (StringUtils.hasText(githubUser.getNickname())) {
            return githubUser.getNickname();
        }
        return githubUser.getGithubLogin();
    }

    private String resolveNickname(GithubRegistrationSession registrationSession)
    {
        if (StringUtils.hasText(registrationSession.getNickname())) {
            return registrationSession.getNickname();
        }
        return registrationSession.getGithubLogin();
    }

    private String resolveBio(GithubOAuthUserDTO githubUser)
    {
        if (StringUtils.hasText(githubUser.getBio())) {
            return githubUser.getBio();
        }
        return githubUser.getGithubLogin() + " 的 GitHub 主页";
    }

    private String resolveBio(GithubRegistrationSession registrationSession)
    {
        if (StringUtils.hasText(registrationSession.getBio())) {
            return registrationSession.getBio();
        }
        return registrationSession.getGithubLogin() + " 的 GitHub 主页";
    }

    private UserAccount requireActiveUser(String username)
    {
        UserAccount userAccount = findByUsername(username);
        if (userAccount == null || Boolean.FALSE.equals(userAccount.getStatus())) {
            throw new IllegalArgumentException("当前用户不存在或已失效");
        }
        return userAccount;
    }

    private UserAccount requireActiveUser(Long userId)
    {
        UserAccount userAccount = userAccountMapper.selectById(userId);
        if (userAccount == null || Boolean.FALSE.equals(userAccount.getStatus())) {
            throw new IllegalArgumentException("用户不存在或已删除");
        }
        return userAccount;
    }

    private Long safeAdminCount()
    {
        Long count = userAccountMapper.countActiveAdmins();
        return count == null ? 0L : count;
    }

    private boolean isAdmin(UserAccount userAccount)
    {
        return userAccount != null && "ADMIN".equalsIgnoreCase(userAccount.getRole());
    }

    private String buildDeletedUsername(Long userId)
    {
        return "deleted_user_" + userId + "_" + System.currentTimeMillis();
    }

    private AdminUserVO toAdminUserVO(UserAccount userAccount)
    {
        return new AdminUserVO(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getNickname(),
                userAccount.getAvatar(),
                userAccount.getEmail(),
                userAccount.getPhone(),
                userAccount.getBio(),
                userAccount.getRole(),
                userAccount.getGithubLogin(),
                StringUtils.hasText(userAccount.getPasswordHash()),
                userAccount.getStatus(),
                userAccount.getCreateTime(),
                userAccount.getLastLoginTime()
        );
    }
}
