package com.training.system.info.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 教师信息只读 Mapper
 */
@Mapper
public interface TeacherMapper {

    @Select("SELECT teacher_name FROM teacher WHERE teacher_id = #{teacherId}")
    String selectTeacherNameById(@Param("teacherId") Long teacherId);

    @Select("<script>" +
            "SELECT teacher_id, teacher_name FROM teacher " +
            "WHERE teacher_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> selectTeacherNamesByIds(@Param("ids") List<Long> ids);
}
