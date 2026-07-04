package com.training.system.topic.mapper;

import com.training.system.topic.entity.TopicReview;
import com.training.system.topic.vo.TopicReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目审核记录 Mapper
 */
@Mapper
public interface TopicReviewMapper {

    /**
     * 新增审核记录
     */
    int insert(TopicReview review);

    /**
     * 查询题目的审核记录列表
     */
    List<TopicReviewVO> selectByTopicId(@Param("topicId") Long topicId);
}
