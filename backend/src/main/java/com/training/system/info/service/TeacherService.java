package com.training.system.info.service;

import com.training.system.common.PageResult;
import com.training.system.info.dto.TeacherCreateDTO;
import com.training.system.info.dto.TeacherUpdateDTO;
import com.training.system.info.entity.Teacher;
import com.training.system.info.vo.TeacherVO;

import java.util.List;

/**
 * 教师管理服务接口
 */
public interface TeacherService {

    TeacherVO createTeacher(TeacherCreateDTO dto);

    TeacherVO updateTeacher(Long teacherId, TeacherUpdateDTO dto);

    PageResult<TeacherVO> pageTeachers(String keyword, int pageNum, int pageSize);

    TeacherVO getTeacherDetail(Long teacherId);

    void resetPassword(Long teacherId);

    void toggleRole(Long teacherId);

    void deleteTeacher(Long teacherId);

    List<Teacher> getAllTeachers();

    void updateSelfPassword(Long teacherId, String oldPwd, String newPwd);

    long count();
}
