package com.training.system.score.mapper;

import com.training.system.score.entity.StageTask;
import com.training.system.score.vo.StageTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StageTaskMapper {
    int insert(StageTask stageTask);

    StageTask selectById(@Param("stageId") Long stageId);

    List<StageTaskVO> selectPage(@Param("keyword") String keyword,
                                 @Param("teacherId") Long teacherId,
                                 @Param("status") Integer status,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword,
                   @Param("teacherId") Long teacherId,
                   @Param("status") Integer status);

    int countAll();
}

