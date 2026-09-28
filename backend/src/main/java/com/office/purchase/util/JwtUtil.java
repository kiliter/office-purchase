package com.office.purchase.util;

import com.office.purchase.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录令牌工具。令牌里只放用户编号和角色，权限仍以数据库中的最新角色为准。
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${purchase.jwt-secret:}")
    private String secret;

    @Value("${purchase.jwt-expire-hours}")
    private int expireHours;

    /**
     * 密钥来自环境变量 PURCHASE_JWT_SECRET。未配置时只在本次启动生成临时密钥，不写入仓库。
     */
    @PostConstruct
    public void initSecret() {
        if (!StringUtils.hasText(secret)) {
            secret = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            log.warn("未配置 PURCHASE_JWT_SECRET，已生成本次启动专用的临时密钥。重启后需要重新登录。");
        }
    }

    /**
     * 为登录成功的用户签发令牌。
     */
    public String createToken(User user) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole());
        long expireAt = System.currentTimeMillis() + expireHours * 3600L * 1000L;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(expireAt))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 解析令牌。过期或签名不对时由调用方转换成未登录。
     */
    public Claims parse(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
