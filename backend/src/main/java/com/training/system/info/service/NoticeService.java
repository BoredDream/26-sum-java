package com.training.system.info.service;

import com.training.system.common.PageResult;
import com.training.system.info.dto.NoticeCreateDTO;
import com.training.system.info.dto.NoticeQueryDTO;
import com.training.system.info.dto.NoticeUpdateDTO;
import com.training.system.info.vo.NoticeVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 公告管理服务接口
 */
public interface NoticeService {

    NoticeVO createNotice(NoticeCreateDTO dto, Long publisherId, MultipartFile file);

    NoticeVO updateNotice(Long noticeId, NoticeUpdateDTO dto, MultipartFile file);

    void deleteNotice(Long noticeId, Long operatorId, boolean isAdmin);

    NoticeVO getNoticeDetail(Long noticeId);

    PageResult<NoticeVO> pageNotices(NoticeQueryDTO query);

    void toggleTop(Long noticeId);

    String getAttachmentPath(Long noticeId);

    long count();

    /**
     * 获取当前学生的未读公告数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记某条公告为已读
     */
    void markAsRead(Long noticeId, Long userId);

    /**
     * 获取用户已读的公告ID列表
     */
    java.util.List<Long> getReadNoticeIds(Long userId);
}
