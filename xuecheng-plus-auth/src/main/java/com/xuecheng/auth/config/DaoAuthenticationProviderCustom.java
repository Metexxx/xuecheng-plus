package com.xuecheng.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @author Swith4Sumin
 * @version 1.0
 * @description 自定义认证逻辑
 * @date 2025/3/4 11:29
 */
@Slf4j
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    // 由于DaoAuthenticationProvider调用UserDetailsService，所以这里需要注入一个
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService){
        super.setUserDetailsService(userDetailsService);
    }

    // 屏蔽密码对比，因为不是所有的认证方式都需要校验密码
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // 里面啥也不写就不会校验密码了
    }
}
