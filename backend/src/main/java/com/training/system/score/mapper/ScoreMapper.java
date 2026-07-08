package com.training.system.score.mapper;

import com.training.system.score.entity.ScoreRecord;
import com.training.system.score.entity.StudentScore;
import com.training.system.score.vo.ProgressVO;
import com.training.system.score.vo.ScoreVO;
import com.training.system.score.vo.StudentScoreVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScoreMapper {
    ScoreRecord selectById(@Param("scoreId") Long scoreId);

    ScoreRecord selectByTeamId(@Param("teamId") Long teamId);

    int insertScore(ScoreRecord score);

    int updateScore(ScoreRecord score);

    int updateScoreStatus(@Param("scoreId") Long scoreId, @Param("status") Integer status);

    int deleteStudentScoresByScoreId(@Param("scoreId") Long scoreId);

    int insertStudentScore(StudentScore studentScore);

    List<ScoreVO> selectScorePage(@Param("keyword") String keyword,
                                  @Param("teamId") Long teamId,
                                  @Param("teacherId") Long teacherId,
                                  @Param("status") Integer status,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    long countScorePage(@Param("keyword") String keyword,
                        @Param("teamId") Long teamId,
                        @Param("teacherId") Long teacherId,
                        @Param("status") Integer status);

    List<StudentScoreVO> selectStudentScores(@Param("studentId") Long studentId);

    List<ProgressVO> selectProgress(@Param("teamId") Long teamId, @Param("teacherId") Long teacherId);

    List<ScoreVO> selectScoreExport(@Param("teacherId") Long teacherId);
}

