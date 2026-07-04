package com.training.system.info.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.info.dto.TeacherCreateDTO;
import com.training.system.info.entity.Teacher;
import com.training.system.info.entity.UserAccount;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.TeacherMapper;
import com.training.system.info.mapper.UserAccountMapper;
import com.training.system.info.service.TeacherService;
import com.training.system.info.vo.TeacherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public TeacherVO createTeacher(TeacherCreateDTO dto) {
        if (teacherMapper.selectByTeacherNo(dto.getTeacherNo()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该工号已存在");
        }
        Teacher teacher = new Teacher();
        teacher.setTeacherNo(dto.getTeacherNo());
        teacher.setTeacherName(dto.getTeacherName());
        teacher.setOffice(dto.getOffice());
        teacher.setTitle(dto.getTitle());
        teacher.setPhone(dto.getPhone());
        teacher.setEmail(dto.getEmail());
        teacherMapper.insert(teacher);

        UserAccount account = new UserAccount();
        account.setUsername(dto.getTeacherNo());
        account.setPassword(encoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        account.setRole("TEACHER");
        account.setRelatedId(teacher.getTeacherId());
        account.setStatus(1);
        userAccountMapper.insert(account);

        return toVO(teacher, account);
    }

    @Override
    @Transactional
    public TeacherVO updateTeacher(Long teacherId, TeacherCreateDTO dto) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师不存在");
        }
        if (dto.getTeacherName() != null) teacher.setTeacherName(dto.getTeacherName());
        if (dto.getOffice() != null) teacher.setOffice(dto.getOffice());
        if (dto.getTitle() != null) teacher.setTitle(dto.getTitle());
        if (dto.getPhone() != null) teacher.setPhone(dto.getPhone());
        if (dto.getEmail() != null) teacher.setEmail(dto.getEmail());
        teacherMapper.update(teacher);
        return getTeacherDetail(teacherId);
    }

    @Override
    public PageResult<TeacherVO> pageTeachers(String keyword, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<TeacherVO> records = teacherMapper.selectPage(keyword, offset, pageSize);
        long total = teacherMapper.countPage(keyword);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public TeacherVO getTeacherDetail(Long teacherId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师不存在");
        }
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "TEACHER");
        if (account == null) {
            account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "ADMIN");
        }
        return toVO(teacher, account);
    }

    @Override
    @Transactional
    public void resetPassword(Long teacherId) {
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "TEACHER");
        if (account == null) {
            account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "ADMIN");
        }
        if (account == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师账号不存在");
        }
        userAccountMapper.updatePassword(account.getUserId(), encoder.encode("123456"));
    }

    @Override
    @Transactional
    public void toggleRole(Long teacherId) {
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "TEACHER");
        if (account == null) {
            account = userAccountMapper.selectByRelatedIdAndRole(teacherId, "ADMIN");
        }
        if (account == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师账号不存在");
        }
        String newRole = "ADMIN".equals(account.getRole()) ? "TEACHER" : "ADMIN";
        account.setRole(newRole);
        userAccountMapper.updateStatus(account.getUserId(), account.getStatus());
    }

    @Override
    public long count() {
        return teacherMapper.countPage(null);
    }

    private TeacherVO toVO(Teacher teacher, UserAccount account) {
        TeacherVO vo = new TeacherVO();
        vo.setTeacherId(teacher.getTeacherId());
        vo.setTeacherNo(teacher.getTeacherNo());
        vo.setTeacherName(teacher.getTeacherName());
        vo.setOffice(teacher.getOffice());
        vo.setTitle(teacher.getTitle());
        vo.setPhone(teacher.getPhone());
        vo.setEmail(teacher.getEmail());
        vo.setCreateTime(teacher.getCreateTime());
        if (account != null) {
            vo.setRole(account.getRole());
            vo.setRoleText("ADMIN".equals(account.getRole()) ? "管理员" : "教师");
        }
        return vo;
    }
}
