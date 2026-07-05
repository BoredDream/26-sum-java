package com.training.system.info.mapper;

import com.training.system.info.entity.Teacher;
import com.training.system.info.vo.TeacherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper {

    int insert(Teacher teacher);

    int update(Teacher teacher);

    Teacher selectById(@Param("teacherId") Long teacherId);

    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);

    List<TeacherVO> selectPage(@Param("keyword") String keyword,
                               @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("keyword") String keyword);

    @Select("<script>SELECT teacher_id, teacher_name FROM teacher WHERE teacher_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectTeacherNamesByIds(@Param("ids") List<Long> ids);
}
