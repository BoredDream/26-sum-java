package com.training.system.selection.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队成员只读 Mapper
 */
@Mapper
public interface TeamMemberMapper {

    @Select("SELECT student_id FROM team_member WHERE team_id = #{teamId} AND status = 1")
    List<Long> selectStudentIdsByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT COUNT(*) FROM team_info WHERE team_id = #{teamId}")
    int countTeamById(@Param("teamId") Long teamId);
}
