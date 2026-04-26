package com.slow.excel_tools_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slow.excel_tools_backend.common.JwtUtil;
import com.slow.excel_tools_backend.common.Result;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 * <p>
 * 校验 Authorization 头中的 JWT token，将 userId 注入请求属性
 * </p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 导出接口不鉴权
        String uri = request.getRequestURI();
        if (uri.contains("/export")) {
            return true;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isEmpty()) {
            writeError(response, 4001, "未登录，请先登录");
            return false;
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Claims claims = JwtUtil.parse(token);
        if (claims == null) {
            writeError(response, 4002, "登录已过期，请重新登录");
            return false;
        }

        // 将 userId 注入请求属性，Controller 中可直接获取
        request.setAttribute("userId", Long.parseLong(claims.getSubject()));
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
    }
}
