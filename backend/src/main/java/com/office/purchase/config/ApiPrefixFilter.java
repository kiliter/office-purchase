package com.office.purchase.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 演示镜像里页面和接口在同一个服务上。
 * 已打包的前端仍请求 /api 前缀，而控制器路径没有这个前缀。
 * 开发时 Vite 会去掉 /api；这里在后端做同样的剥离，避免登录被报成网络异常。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiPrefixFilter extends OncePerRequestFilter {

    /** 只处理这一层前缀，避免误伤其它路径。 */
    private static final String API_PREFIX = "/api";

    /**
     * /api 与 /api/** 转成去掉前缀后的路径，其它请求原样继续。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        if (!path.equals(API_PREFIX) && !path.startsWith(API_PREFIX + "/")) {
            chain.doFilter(request, response);
            return;
        }
        // "/api" 变成 "/"，"/api/user/login" 变成 "/user/login"
        String stripped = path.length() == API_PREFIX.length() ? "/" : path.substring(API_PREFIX.length());
        chain.doFilter(new PrefixStrippedRequest(request, contextPath, stripped), response);
    }

    /**
     * Spring MVC 用请求 URI 匹配控制器，所以要同时改掉 URI 和 servletPath。
     */
    private static final class PrefixStrippedRequest extends HttpServletRequestWrapper {

        private final String contextPath;
        private final String servletPath;

        private PrefixStrippedRequest(HttpServletRequest request, String contextPath, String servletPath) {
            super(request);
            this.contextPath = contextPath;
            this.servletPath = servletPath;
        }

        @Override
        public String getRequestURI() {
            return contextPath + servletPath;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        /** 路径已经完整放在 servletPath 中，不再额外拆 pathInfo。 */
        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            int port = getServerPort();
            if (port > 0 && (("http".equals(getScheme()) && port != 80) || ("https".equals(getScheme()) && port != 443))) {
                url.append(':').append(port);
            }
            url.append(getRequestURI());
            return url;
        }
    }
}
