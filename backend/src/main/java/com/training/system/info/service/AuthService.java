package com.training.system.info.service;

import com.training.system.info.dto.LoginDTO;
import com.training.system.info.vo.LoginVO;
import jakarta.servlet.http.HttpSession;

/**
 * 认证服务接口
 */
public interface AuthService {

    LoginVO login(LoginDTO dto, HttpSession session);

    void logout(HttpSession session);
}
