package blog.controller.admin;

import blog.dto.UserDTO;
import blog.result.Result;
import blog.service.UserService;
import blog.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminUserController")
@RequestMapping("/v1/auth")  // 注意这里是 /v1/auth
@Slf4j
@Api(tags = "用户管理页面")  // 用 @Api 而不是 @ApiOperation
public class UserController
{
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")  // 只保留这一个注解
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@RequestBody UserDTO userDTO) {
        log.info("用户登录: {}", userDTO);
        return userService.login(userDTO);
    }

    /**
     * 测试接口（验证Controller是否工作）
     */
    @GetMapping("/test")
    @ApiOperation("测试接口")
    public String test() {
        return "UserController is working!";
    }
}