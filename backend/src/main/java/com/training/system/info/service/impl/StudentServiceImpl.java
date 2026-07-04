package com.training.system.info.service.impl;

import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.info.dto.StudentCreateDTO;
import com.training.system.info.entity.Student;
import com.training.system.info.entity.UserAccount;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.StudentMapper;
import com.training.system.info.mapper.UserAccountMapper;
import com.training.system.info.service.StudentService;
import com.training.system.info.vo.StudentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public StudentVO createStudent(StudentCreateDTO dto) {
        if (studentMapper.selectByStudentNo(dto.getStudentNo()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该学号已存在");
        }
        Student student = new Student();
        student.setStudentNo(dto.getStudentNo());
        student.setStudentName(dto.getStudentName());
        student.setMajor(dto.getMajor());
        student.setClassName(dto.getClassName());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());
        studentMapper.insert(student);

        UserAccount account = new UserAccount();
        account.setUsername(dto.getStudentNo());
        account.setPassword(encoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        account.setRole("STUDENT");
        account.setRelatedId(student.getStudentId());
        account.setStatus(1);
        userAccountMapper.insert(account);

        return toVO(student, account);
    }

    @Override
    @Transactional
    public StudentVO updateStudent(Long studentId, StudentCreateDTO dto) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
        }
        if (dto.getStudentName() != null) student.setStudentName(dto.getStudentName());
        if (dto.getMajor() != null) student.setMajor(dto.getMajor());
        if (dto.getClassName() != null) student.setClassName(dto.getClassName());
        if (dto.getPhone() != null) student.setPhone(dto.getPhone());
        if (dto.getEmail() != null) student.setEmail(dto.getEmail());
        studentMapper.update(student);
        return getStudentDetail(studentId);
    }

    @Override
    public PageResult<StudentVO> pageStudents(String keyword, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<StudentVO> records = studentMapper.selectPage(keyword, status, offset, pageSize);
        long total = studentMapper.countPage(keyword, status);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public StudentVO getStudentDetail(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
        }
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(studentId, "STUDENT");
        return toVO(student, account);
    }

    @Override
    @Transactional
    public void resetPassword(Long studentId) {
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(studentId, "STUDENT");
        if (account == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生账号不存在");
        }
        userAccountMapper.updatePassword(account.getUserId(), encoder.encode("123456"));
    }

    @Override
    @Transactional
    public void toggleStatus(Long studentId) {
        UserAccount account = userAccountMapper.selectByRelatedIdAndRole(studentId, "STUDENT");
        if (account == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学生账号不存在");
        }
        int newStatus = account.getStatus() == 1 ? 0 : 1;
        userAccountMapper.updateStatus(account.getUserId(), newStatus);
    }

    @Override
    public long count() {
        return studentMapper.countPage(null, null);
    }

    private StudentVO toVO(Student student, UserAccount account) {
        StudentVO vo = new StudentVO();
        vo.setStudentId(student.getStudentId());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getStudentName());
        vo.setMajor(student.getMajor());
        vo.setClassName(student.getClassName());
        vo.setPhone(student.getPhone());
        vo.setEmail(student.getEmail());
        vo.setCreateTime(student.getCreateTime());
        if (account != null) {
            vo.setStatus(account.getStatus());
            vo.setStatusText(account.getStatus() == 1 ? "正常" : "禁用");
        }
        return vo;
    }
}
