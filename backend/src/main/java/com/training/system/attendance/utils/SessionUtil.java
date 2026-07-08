package com.training.system.attendance.utils;

import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import jakarta.servlet.http.HttpSession;

/**
 * 当前登录用户工具类
 */
public class SessionUtil {

    private SessionUtil() {
    }

    public static CurrentUserDTO getCurrentUser(HttpSession session) {
        if (session == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = toLong(session.getAttribute("userId"));
        String role = (String) session.getAttribute("role");
        Long relatedId = toLong(session.getAttribute("relatedId"));
        if (userId == null || role == null || relatedId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        return new CurrentUserDTO(userId, role, relatedId);
    }

    private static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    public static void requireRole(CurrentUserDTO user, String... roles) {
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        for (String role : roles) {
            if (role.equals(user.getRole())) {
                return;
            }
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权限");
    }
}
