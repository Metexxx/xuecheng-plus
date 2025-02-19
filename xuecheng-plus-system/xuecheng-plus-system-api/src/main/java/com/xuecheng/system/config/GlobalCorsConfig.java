package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @description 跨域过虑器
 * @author Swith4Sumin
 * @date 2025/2/18 11:04
 * @version 1.0
 */
 @Configuration
 public class GlobalCorsConfig {
    /**
     * 运行跨域调用的过滤器
     * @return CorsFilter
     */
    @Bean
    public CorsFilter getCorsFilter(){
        System.err.println("CorsFilter注册...");

        CorsConfiguration configuration= new CorsConfiguration();

        //添加哪些http方法可以跨域，比如：GET,Post，（多个方法中间以逗号分隔），*号表示所有
        configuration.addAllowedMethod("*");
        //添加允许哪个请求进行跨域，*表示所有,可以具体指定http://localhost:8601表示只允许http://localhost:8601/跨域
        configuration.addAllowedOrigin("http://localhost:8601");    // 和setAllowCredentials同时存在必须指定地址！！！
        //所有头信息全部放行
        configuration.addAllowedHeader("*");
        //允许跨域发送cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",configuration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
