package com.training.system.topic.mapper;

import com.training.system.topic.entity.ProjectTopic;
import com.training.system.topic.vo.TopicListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目题目 Mapper
 */
@Mapper
public interface ProjectTopicMapper {

    /**
     * 新增题目
     */
    int insert(ProjectTopic topic);

    /**
     * 修改题目
     */
    int update(ProjectTopic topic);

    /**
     * 逻辑删除题目
     */
    int deleteById(@Param("topicId") Long topicId);

    /**
     * 根据ID查询题目（含已删除）
     */
    ProjectTopic selectById(@Param("topicId") Long topicId);

    /**
     * 分页查询题目列表（含教师姓名）
     */
    List<TopicListVO> selectPage(
            @Param("keyword") String keyword,
            @Param("topicType") String topicType,
            @Param("difficulty") String difficulty,
            @Param("status") Integer status,
            @Param("openStatus") Integer openStatus,
            @Param("teacherId") Long teacherId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime,
            @Param("isDeleted") Integer isDeleted,
            @Param("statusList") List<Integer> statusList,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计题目总数
     */
    long countPage(
            @Param("keyword") String keyword,
            @Param("topicType") String topicType,
            @Param("difficulty") String difficulty,
            @Param("status") Integer status,
            @Param("openStatus") Integer openStatus,
            @Param("teacherId") Long teacherId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime,
            @Param("isDeleted") Integer isDeleted,
            @Param("statusList") List<Integer> statusList
    );

    /**
     * 更新题目审核状态
     */
    int updateStatus(@Param("topicId") Long topicId, @Param("status") Integer status);

    /**
     * 更新题目开放状态
     */
    int updateOpenStatus(@Param("topicId") Long topicId, @Param("openStatus") Integer openStatus);

    /**
     * 检查题目是否已被学生选择（存在有效的选题申请）
     */
    int countActiveSelections(@Param("topicId") Long topicId);
}
