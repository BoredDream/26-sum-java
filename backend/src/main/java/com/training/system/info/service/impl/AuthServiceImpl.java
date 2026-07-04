package com.training.system.info.service.impl;

import com.training.system.common.ResultCode;
import com.training.system.info.dto.LoginDTO;
import com.training.system.info.entity.Student;
import com.training.system.info.entity.Teacher;
import com.training.system.info.entity.UserAccount;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.info.mapper.TeacherMapper;
import com.training.system.info.mapper.UserAccountMapper;
import com.training.system.info.service.AuthService;
import com.training.system.info.vo.LoginVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO dto, HttpSession session) {
        UserAccount account = userAccountMapper.selectByUsername(dto.getUsername());
        if (account == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (account.getStatus() != null && account.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        if (!encoder.matches(dto.getPassword(), account.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }

        String name = "";
        if ("STUDENT".equals(account.getRole())) {
            Student student = studentMapper.selectById(account.getRelatedId());
            name = student != null ? student.getStudentName() : "";
        } else {
            Teacher teacher = teacherMapper.selectById(account.getRelatedId());
            name = teacher != null ? teacher.getTeacherName() : "";
        }

        session.setAttribute("userId", account.getUserId());
        session.setAttribute("role", account.getRole());
        session.setAttribute("name", name);

        LoginVO vo = new LoginVO();
        vo.setUserId(account.getUserId());
        vo.setUsername(account.getUsername());
        vo.setRole(account.getRole());
        vo.setName(name);
        return vo;
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
