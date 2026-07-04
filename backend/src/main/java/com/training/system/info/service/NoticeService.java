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

    void deleteNotice(Long noticeId);

    NoticeVO getNoticeDetail(Long noticeId);

    PageResult<NoticeVO> pageNotices(NoticeQueryDTO query);

    void toggleTop(Long noticeId);

    String getAttachmentPath(Long noticeId);
}
