package com.training.system.info.controller;

import com.training.system.common.Result;
import com.training.system.info.annotation.OperationLog;
import com.training.system.info.dto.LoginDTO;
import com.training.system.info.service.AuthService;
import com.training.system.info.vo.LoginVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @OperationLog(type = "LOGIN", description = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpSession session) {
        LoginVO vo = authService.login(dto, session);
        return Result.success(vo);
    }

    @OperationLog(type = "LOGOUT", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        authService.logout(session);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<LoginVO> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.fail(com.training.system.common.ResultCode.UNAUTHORIZED);
        }
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setRelatedId((Long) session.getAttribute("relatedId"));
        vo.setUsername((String) session.getAttribute("username"));
        vo.setRole((String) session.getAttribute("role"));
        vo.setName((String) session.getAttribute("name"));
        return Result.success(vo);
    }
}
