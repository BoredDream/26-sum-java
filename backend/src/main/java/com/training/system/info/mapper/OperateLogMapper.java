package com.training.system.info.mapper;

import com.training.system.info.entity.OperateLog;
import com.training.system.info.vo.OperateLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperateLogMapper {

    int insert(OperateLog log);

    List<OperateLogVO> selectPage(@Param("keyword") String keyword,
                                  @Param("operateType") String operateType,
                                  @Param("offset") int offset, @Param("size") int size);

    long countPage(@Param("keyword") String keyword, @Param("operateType") String operateType);

    int deleteByTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}
