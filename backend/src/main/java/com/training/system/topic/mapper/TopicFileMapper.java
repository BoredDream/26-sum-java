package com.training.system.topic.mapper;

import com.training.system.topic.entity.TopicFile;
import com.training.system.topic.vo.TopicFileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目资料文件 Mapper
 */
@Mapper
public interface TopicFileMapper {

    /**
     * 新增文件记录
     */
    int insert(TopicFile file);

    /**
     * 逻辑删除文件
     */
    int deleteById(@Param("fileId") Long fileId);

    /**
     * 根据ID查询文件
     */
    TopicFile selectById(@Param("fileId") Long fileId);

    /**
     * 查询题目下的文件列表
     */
    List<TopicFileVO> selectByTopicId(@Param("topicId") Long topicId);
}
