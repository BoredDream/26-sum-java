package com.training.system.info.mapper;

import com.training.system.info.entity.Student;
import com.training.system.info.vo.StudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper {

    int insert(Student student);

    int update(Student student);

    Student selectById(@Param("studentId") Long studentId);

    Student selectByStudentNo(@Param("studentNo") String studentNo);

    List<StudentVO> selectPage(@Param("keyword") String keyword, @Param("status") Integer status,
                               @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("keyword") String keyword, @Param("status") Integer status);

    @Select("<script>SELECT * FROM student WHERE student_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Student> selectByIds(@Param("ids") List<Long> ids);

    @Select("SELECT student_id FROM student WHERE class_name = #{className}")
    List<Long> selectIdsByClassName(@Param("className") String className);

    @Select("SELECT student_id FROM student")
    List<Long> selectAllIds();
}
