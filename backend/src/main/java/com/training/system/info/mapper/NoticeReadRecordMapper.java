package com.training.system.info.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告阅读记录 Mapper
 */
@Mapper
public interface NoticeReadRecordMapper {

    /**
     * 插入阅读记录（INSERT IGNORE，重复阅读不报错）
     */
    int insert(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /**
     * 统计当前用户未读的公告数量
     */
    long countUnread(@Param("userId") Long userId);

    /**
     * 查询当前用户已读的公告ID列表
     */
    List<Long> selectReadNoticeIds(@Param("userId") Long userId);
}
