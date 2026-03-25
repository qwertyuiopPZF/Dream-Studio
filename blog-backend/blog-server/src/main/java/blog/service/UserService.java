package blog.service;

import blog.dto.UserDTO;
import blog.result.Result;
import blog.vo.LoginVO;
import org.springframework.stereotype.Service;

@Service
public interface UserService
{
    /*
    用户登录
     */
    Result<LoginVO> login(UserDTO userDTO);
}
