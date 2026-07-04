package com.training.system.topic.service;

import com.training.system.common.PageResult;
import com.training.system.topic.dto.TopicCreateDTO;
import com.training.system.topic.dto.TopicQueryDTO;
import com.training.system.topic.dto.TopicReviewDTO;
import com.training.system.topic.dto.TopicUpdateDTO;
import com.training.system.topic.vo.TopicDetailVO;
import com.training.system.topic.vo.TopicFileVO;
import com.training.system.topic.vo.TopicListVO;
import com.training.system.topic.vo.TopicReviewVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 出题管理服务接口
 */
public interface TopicService {

    /**
     * 新增题目（保存草稿或提交审核）
     */
    void createTopic(TopicCreateDTO dto, Long userId, String role, Long relatedId);

    /**
     * 修改题目
     */
    void updateTopic(Long topicId, TopicUpdateDTO dto, Long userId, String role, Long relatedId);

    /**
     * 删除题目（逻辑删除，仅管理员）
     */
    void deleteTopic(Long topicId, Long userId, String role);

    /**
     * 查看题目详情
     */
    TopicDetailVO getTopicDetail(Long topicId, Long userId, String role, Long relatedId);

    /**
     * 分页查询题目列表
     */
    PageResult<TopicListVO> queryTopicPage(TopicQueryDTO dto, Long userId, String role, Long relatedId);

    /**
     * 提交题目审核
     */
    void submitForReview(Long topicId, Long userId, String role, Long relatedId);

    /**
     * 审核题目（仅管理员）
     */
    void reviewTopic(Long topicId, TopicReviewDTO dto, Long userId);

    /**
     * 开放题目（仅管理员）
     */
    void openTopic(Long topicId, Long userId, String role);

    /**
     * 关闭题目（仅管理员）
     */
    void closeTopic(Long topicId, Long userId, String role);

    /**
     * 上传题目资料
     */
    void uploadFile(Long topicId, MultipartFile file, String fileDesc, Long userId, String role, Long relatedId);

    /**
     * 删除资料（逻辑删除）
     */
    void deleteFile(Long fileId, Long userId, String role, Long relatedId);

    /**
     * 查看题目资料列表
     */
    List<TopicFileVO> listFiles(Long topicId, Long userId, String role, Long relatedId);

    /**
     * 下载资料文件（返回文件 Resource 和原始文件名）
     */
    Resource downloadFile(Long fileId, Long userId, String role, Long relatedId);

    /**
     * 获取文件原始名称
     */
    String getFileOriginalName(Long fileId, Long userId, String role, Long relatedId);

    /**
     * 查看题目审核记录
     */
    List<TopicReviewVO> listReviews(Long topicId, Long userId, String role, Long relatedId);
}
