package com.code.codeagent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")    // 拦截的 path 列表，可以写多个 */
                    .notMatch("/user/login")        // 排除用户登录接口
                    .notMatch("/user/register")     // 排除用户注册接口
                    .notMatch("/auth/**")           // 排除认证状态查询接口
                    .notMatch("/example/**")        // 排除示例接口
                    .notMatch("/swagger-ui/**")     // 排除 Swagger UI
                    .notMatch("/swagger-ui.html")   // 排除 Swagger UI
                    .notMatch("/v3/api-docs/**")    // 排除 API 文档
                    .notMatch("/doc.html")          // 排除 knife4j 文档
                    .notMatch("/webjars/**")        // 排除静态资源
                    .notMatch("/favicon.ico")       // 排除图标
                    .notMatch("/error")             // 排除错误页面
                    .check(r -> StpUtil.checkLogin());        // 登录校验 -- 拦截所有路由，并排除指定接口
        })).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 