package blog.service.impl;

import blog.entity.UserAccount;
import blog.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service

public class UserDetailsServiceImpl implements UserDetailsService
{
    @Autowired
    private UserAccountMapper userAccountMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserAccount userAccount = userAccountMapper.selectByUsername(username);
        if (userAccount == null || Boolean.FALSE.equals(userAccount.getStatus())) {
            throw new UsernameNotFoundException("用户名不存在: " + username);
        }

        if (!StringUtils.hasText(userAccount.getPasswordHash())) {
            throw new UsernameNotFoundException("当前账号未设置站内密码: " + username);
        }

        return User.builder()
                .username(userAccount.getUsername())
                .password(userAccount.getPasswordHash())
                .authorities("ROLE_" + userAccount.getRole().toUpperCase())
                .build();
    }

}
