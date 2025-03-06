package com.xuecheng.content.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @author Administrator
 * @version 1.0
 **/
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    //资源服务标识
    public static final String RESOURCE_ID = "xuecheng-plus";

    @Autowired
    TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID)//资源 id
                .tokenStore(tokenStore)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()           // 禁用 CSRF 保护
                .authorizeRequests()
//                .antMatchers("/r/**","/course/**") // 令牌在网关处校验 不在微服务校验了 因此这里注释掉
//                .authenticated()    // 指定 "/r/" 和 "/course/" 这两个路径需要进行身份认证才能访问。
                .anyRequest()
                .permitAll();       // 允许所有其他请求（除了上面指定的路径之外）都可以被访问，不需要进行身份认证
    }
}
