package blog.controller.admin;

import blog.result.Result;
import blog.service.AccessControlService;
import blog.service.UserAccountService;
import blog.vo.AdminUserVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminUserManagementController")
@RequestMapping("/admin/users")
public class UserManagementController
{
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    @ApiOperation("获取后台用户列表")
    public Result<List<AdminUserVO>> listUsers(Authentication authentication)
    {
        try {
            accessControlService.requireAdmin(requireUsername(authentication));
            return Result.success(userAccountService.listActiveUsers());
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @PutMapping("/{id}/admin-status")
    @ApiOperation("更新用户管理员状态")
    public Result<AdminUserVO> updateAdminStatus(@PathVariable Long id,
                                                 @RequestParam Boolean admin,
                                                 Authentication authentication)
    {
        try {
            return Result.success(userAccountService.updateUserAdminRole(id, admin, requireUsername(authentication)));
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<Void> deleteUser(@PathVariable Long id,
                                   Authentication authentication)
    {
        try {
            userAccountService.deleteUser(id, requireUsername(authentication));
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(403, e.getMessage());
        }
    }

    private String requireUsername(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return authentication.getName();
    }
}
