package com.office.purchase.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.UserContext;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import com.office.purchase.controller.SpaController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 登录与角色拦截。静态页面放行，业务接口必须携带有效令牌。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 校验令牌，并把当前用户放入线程上下文。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }
        // 页面路由转发给 index.html，不能按接口做登录拦截
        if (((HandlerMethod) handler).getBean() instanceof SpaController) {
            return true;
        }
        String header = request.getHeader("Authorization");
        String token = resolveToken(header);
        if (token == null || token.isEmpty()) {
            write(response, 401, "请先登录");
            return false;
        }
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception ex) {
            write(response, 401, "登录已过期，请重新登录");
            return false;
        }
        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));
        User user = userMapper.selectById(userId);
        if (user == null) {
            write(response, 401, "账号不存在或已被删除");
            return false;
        }
        HandlerMethod method = (HandlerMethod) handler;
        RequireRole role = method.getMethodAnnotation(RequireRole.class);
        if (role == null) {
            role = method.getBeanType().getAnnotation(RequireRole.class);
        }
        if (role != null && !Arrays.asList(role.value()).contains(user.getRole())) {
            write(response, 403, "没有权限执行此操作");
            return false;
        }
        UserContext.set(user);
        return true;
    }

    /**
     * 请求结束时清理线程变量，避免线程池复用串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 兼容 “Bearer token” 和直接传 token 两种写法。
     */
    private String resolveToken(String header) {
        if (header == null) {
            return null;
        }
        String value = header.trim();
        if (value.startsWith("Bearer ")) {
            return value.substring(7).trim();
        }
        return value;
    }

    /**
     * 拦截器里还没进入控制器，需要自己写 JSON。HTTP 状态保持 200，业务码放在 body.code。
     */
    private void write(HttpServletResponse response, int code, String msg) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
    }
}
