package com.training.system.info.service.impl;

import com.training.system.exception.BusinessException;
import com.training.system.info.dto.LoginDTO;
import com.training.system.info.entity.Student;
import com.training.system.info.entity.Teacher;
import com.training.system.info.entity.UserAccount;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.info.mapper.TeacherMapper;
import com.training.system.info.mapper.UserAccountMapper;
import com.training.system.info.vo.LoginVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private TeacherMapper teacherMapper;
    @Mock
    private HttpSession session;

    private AuthServiceImpl service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl();
        ReflectionTestUtils.setField(service, "userAccountMapper", userAccountMapper);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "teacherMapper", teacherMapper);
    }

    @ParameterizedTest
    @CsvSource({
            "teacher01,TEACHER,STUDENT",
            "teacher01,TEACHER,ADMIN",
            "student01,STUDENT,TEACHER",
            "student01,STUDENT,ADMIN",
            "admin01,ADMIN,STUDENT",
            "admin01,ADMIN,TEACHER"
    })
    @DisplayName("登录 - 选择角色必须与账号角色一致")
    void login_roleMismatchThrows(String username, String actualRole, String selectedRole) {
        when(userAccountMapper.selectByUsername(username)).thenReturn(account(username, actualRole));

        LoginDTO dto = loginDto(username, "123456", selectedRole);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(dto, session));

        assertEquals("用户名或密码错误", ex.getMessage());
        verify(session, never()).setAttribute(anyString(), any());
        verifyNoInteractions(teacherMapper, studentMapper);
    }

    @Test
    @DisplayName("登录 - 教师角色正确时写入账号真实角色")
    void login_teacherRoleMatchesSuccess() {
        when(userAccountMapper.selectByUsername("teacher01")).thenReturn(account("teacher01", "TEACHER"));
        Teacher teacher = new Teacher();
        teacher.setTeacherId(10L);
        teacher.setTeacherName("张老师");
        when(teacherMapper.selectById(10L)).thenReturn(teacher);

        LoginVO vo = service.login(loginDto("teacher01", "123456", "TEACHER"), session);

        assertEquals("TEACHER", vo.getRole());
        assertEquals("张老师", vo.getName());
        verify(session).setAttribute("role", "TEACHER");
        verify(session).setAttribute("relatedId", 10L);
    }

    @Test
    @DisplayName("登录 - 学生角色正确时写入学生姓名")
    void login_studentRoleMatchesSuccess() {
        when(userAccountMapper.selectByUsername("student01")).thenReturn(account("student01", "STUDENT"));
        Student student = new Student();
        student.setStudentId(10L);
        student.setStudentName("张三");
        when(studentMapper.selectById(10L)).thenReturn(student);

        LoginVO vo = service.login(loginDto("student01", "123456", "STUDENT"), session);

        assertEquals("STUDENT", vo.getRole());
        assertEquals("张三", vo.getName());
        verify(session).setAttribute("role", "STUDENT");
        verify(session).setAttribute("relatedId", 10L);
    }

    @Test
    @DisplayName("登出 - 使当前 session 失效")
    void logout_invalidatesSession() {
        service.logout(session);
        verify(session).invalidate();
    }

    @Test
    @DisplayName("登录 - 用户名不存在时提示错误")
    void login_usernameNotFoundThrows() {
        when(userAccountMapper.selectByUsername("notfound")).thenReturn(null);

        LoginDTO dto = loginDto("notfound", "123456", "STUDENT");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(dto, session));
        assertEquals("用户名或密码错误", ex.getMessage());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("登录 - 密码错误时提示错误")
    void login_wrongPasswordThrows() {
        when(userAccountMapper.selectByUsername("student01")).thenReturn(account("student01", "STUDENT"));

        LoginDTO dto = loginDto("student01", "wrongpassword", "STUDENT");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(dto, session));
        assertEquals("用户名或密码错误", ex.getMessage());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("登录 - 账号被禁用时提示禁用")
    void login_disabledAccountThrows() {
        UserAccount account = account("student01", "STUDENT");
        account.setStatus(0);
        when(userAccountMapper.selectByUsername("student01")).thenReturn(account);

        LoginDTO dto = loginDto("student01", "123456", "STUDENT");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(dto, session));
        assertEquals("账号已被禁用", ex.getMessage());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("登录 - 管理员成功登录并写入全部 session 属性")
    void login_adminSuccess_andSetsAllSessionAttributes() {
        when(userAccountMapper.selectByUsername("admin01")).thenReturn(account("admin01", "ADMIN"));
        Teacher teacher = new Teacher();
        teacher.setTeacherId(10L);
        teacher.setTeacherName("系统管理员");
        when(teacherMapper.selectById(10L)).thenReturn(teacher);

        LoginVO vo = service.login(loginDto("admin01", "123456", "ADMIN"), session);

        assertEquals("ADMIN", vo.getRole());
        assertEquals("系统管理员", vo.getName());
        verify(session).setAttribute("userId", 1L);
        verify(session).setAttribute("username", "admin01");
        verify(session).setAttribute("role", "ADMIN");
        verify(session).setAttribute("relatedId", 10L);
        verify(session).setAttribute("name", "系统管理员");
    }

    private UserAccount account(String username, String role) {
        UserAccount account = new UserAccount();
        account.setUserId(1L);
        account.setUsername(username);
        account.setPassword(encoder.encode("123456"));
        account.setRole(role);
        account.setRelatedId(10L);
        account.setStatus(1);
        return account;
    }

    private LoginDTO loginDto(String username, String password, String role) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setRole(role);
        return dto;
    }
}
