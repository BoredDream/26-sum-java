package com.training.system.info.mapper;

import com.training.system.info.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生信息只读 Mapper
 */
@Mapper
public interface StudentMapper {

    @Select("SELECT student_id FROM student WHERE class_name = #{className}")
    List<Long> selectIdsByClassName(@Param("className") String className);

    @Select("SELECT student_id FROM student")
    List<Long> selectAllIds();

    @Select("SELECT * FROM student WHERE student_id = #{studentId}")
    Student selectById(@Param("studentId") Long studentId);

    List<Student> selectByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM student WHERE class_name = #{className}")
    List<Student> selectByClassName(@Param("className") String className);
}
