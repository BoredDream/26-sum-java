package com.training.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.system.common.Result;
import com.training.system.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器
 * 校验所有 /api/** 请求（排除健康检查和登录接口）的 Session 登录状态
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // 放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");

        if (userId == null || role == null) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        return true;
    }

    /**
     * 返回 401 JSON 响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String json = OBJECT_MAPPER.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED, message));
        response.getWriter().write(json);
    }
}
