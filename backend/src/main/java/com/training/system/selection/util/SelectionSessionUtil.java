package com.training.system.selection.util;

import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import jakarta.servlet.http.HttpSession;

public final class SelectionSessionUtil {
    private SelectionSessionUtil() {
    }

    public static CurrentUser currentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        Object relatedId = session.getAttribute("relatedId");
        if (userId == null || role == null || relatedId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        return new CurrentUser((Long) userId, (String) role, (Long) relatedId);
    }

    public record CurrentUser(Long userId, String role, Long relatedId) {
    }
}
