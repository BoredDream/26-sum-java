package com.training.system.info.mapper;

import com.training.system.info.entity.Teacher;
import com.training.system.info.vo.TeacherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    int insert(Teacher teacher);

    int update(Teacher teacher);

    Teacher selectById(@Param("teacherId") Long teacherId);

    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);

    List<TeacherVO> selectPage(@Param("keyword") String keyword,
                               @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("keyword") String keyword);
}
