package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Swith4Sumin
 * @description 自定义UserDetailsService用来对接Spring Security
 * @date 2025/3/1 17:11
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户名不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(username, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", username);
            throw new RuntimeException("认证请求数据格式不对");
        }
        // 获取认证类型，beanName就是 认证类型 + 后缀，例如 password + _authservice = password_authservice
        String authType = authParamsDto.getAuthType();
        // 根据认证类型，从Spring容器中取出对应的bean
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);
        return getUserPrincipal(user);

//        String name = authParamsDto.getUsername();
//        // 从数据库中查询用户信息
//        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, name));
//        if (xcUser == null) {
//            return null;
//        }
//        String password = xcUser.getPassword();
//        // 模拟用户授权; 必须有 否则不能通过空的授权：Cannot pass a null GrantedAuthority collection
//        String[] authorities = {"p1"};
//        // 为了安全, 令牌中不放置密码
//        xcUser.setPassword(null);
//        String userString = JSON.toJSONString(xcUser);
//        // 创建UserDetails对象, 权限信息待实现搜全功能再加入
//        return User.withUsername(userString)
//                .password(password)
//                .authorities(authorities)
//                .build();
    }

    public UserDetails getUserPrincipal(XcUserExt user) {
        // 获取用户id
        String userId = user.getId();
        // 根据用户id查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        ArrayList<String> permissions = new ArrayList<>();
        // 没权限，给一个默认的
        if (xcMenus.isEmpty()) {
            permissions.add("test");
        } else {
            // 获取权限，加入到集合里
            xcMenus.forEach(xcMenu -> {
                permissions.add(xcMenu.getCode());
            });
        }
        // 设置权限
        user.setPermissions(permissions);
        String[] authorities = permissions.toArray(new String[0]);
        String password = user.getPassword();
        user.setPassword(null);
        String userJsonStr = JSON.toJSONString(user);
        return User.withUsername(userJsonStr).password(password).authorities(authorities).build();
    }
}
