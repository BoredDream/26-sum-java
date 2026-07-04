package com.training.system.info.mapper;

import com.training.system.info.entity.Notice;
import com.training.system.info.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("noticeId") Long noticeId);

    Notice selectById(@Param("noticeId") Long noticeId);

    NoticeVO selectVOById(@Param("noticeId") Long noticeId);

    List<NoticeVO> selectPage(@Param("keyword") String keyword, @Param("type") String type,
                              @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("keyword") String keyword, @Param("type") String type);

    int updateTopFlag(@Param("noticeId") Long noticeId, @Param("topFlag") Integer topFlag);
}
