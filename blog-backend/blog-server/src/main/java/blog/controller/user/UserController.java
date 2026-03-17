package blog.controller.user;

import blog.dto.UserPasswordUpdateDTO;
import blog.result.Result;
import blog.service.UserAccountService;
import blog.vo.UserProfileVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController
{
    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/me")
    @ApiOperation("获取当前登录用户资料")
    public Result<UserProfileVO> getCurrentUser(Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        UserProfileVO profile = userAccountService.getProfileByUsername(authentication.getName());
        if (profile == null) {
            return Result.error("未找到当前用户资料");
        }

        return Result.success(profile);
    }

    @PostMapping("/password")
    @ApiOperation("设置当前用户站内密码")
    public Result<Void> updatePassword(@RequestBody UserPasswordUpdateDTO request,
                                       Authentication authentication)
    {
        if (authentication == null || authentication.getName() == null) {
            return Result.error(401, "未登录");
        }

        try {
            userAccountService.updatePassword(authentication.getName(), request.getNewPassword());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
