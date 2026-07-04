package com.training.system.info.mapper;

import com.training.system.info.entity.Student;
import com.training.system.info.vo.StudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
