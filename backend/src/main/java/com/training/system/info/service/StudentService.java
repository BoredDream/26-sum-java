package com.training.system.info.service;

import com.training.system.common.PageResult;
import com.training.system.info.dto.StudentCreateDTO;
import com.training.system.info.vo.StudentVO;

/**
 * 学生管理服务接口
 */
public interface StudentService {

    StudentVO createStudent(StudentCreateDTO dto);

    StudentVO updateStudent(Long studentId, StudentCreateDTO dto);

    PageResult<StudentVO> pageStudents(String keyword, Integer status, int pageNum, int pageSize);

    StudentVO getStudentDetail(Long studentId);

    void resetPassword(Long studentId);

    void toggleStatus(Long studentId);

    void deleteStudent(Long studentId);

    long count();
}
