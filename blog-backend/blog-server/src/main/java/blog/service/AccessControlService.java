package blog.service;

import blog.entity.UserAccount;

public interface AccessControlService
{
    UserAccount requireUser(String username);

    UserAccount requireAdmin(String username);

    UserAccount requireAdminOrOwner(String username, Long ownerId, String resourceName);

    boolean isAdmin(UserAccount userAccount);
}
