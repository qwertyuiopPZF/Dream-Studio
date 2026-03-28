package blog.service.impl;

import blog.entity.UserAccount;
import blog.service.AccessControlService;
import blog.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlServiceImpl implements AccessControlService
{
    @Autowired
    private UserAccountService userAccountService;

    @Override
    public UserAccount requireUser(String username)
    {
        UserAccount userAccount = userAccountService.findByUsername(username);
        if (userAccount == null || Boolean.FALSE.equals(userAccount.getStatus())) {
            throw new IllegalArgumentException("当前用户不存在或已失效");
        }
        return userAccount;
    }

    @Override
    public UserAccount requireAdmin(String username)
    {
        UserAccount userAccount = requireUser(username);
        if (!isAdmin(userAccount)) {
            throw new IllegalArgumentException("当前用户没有管理员权限");
        }
        return userAccount;
    }

    @Override
    public UserAccount requireAdminOrOwner(String username, Long ownerId, String resourceName)
    {
        UserAccount userAccount = requireUser(username);
        if (isAdmin(userAccount)) {
            return userAccount;
        }
        if (ownerId == null || !ownerId.equals(userAccount.getId())) {
            throw new IllegalArgumentException("当前用户没有权限操作" + resourceName);
        }
        return userAccount;
    }

    @Override
    public boolean isAdmin(UserAccount userAccount)
    {
        return userAccount != null && "ADMIN".equalsIgnoreCase(userAccount.getRole());
    }
}
