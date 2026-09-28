package com.office.purchase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 只使用 Spring Security 的密码编码器，不启用整套 Security 过滤器。
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 每次加密结果不同，校验时用 matches，不能对密文再做字符串比较。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
