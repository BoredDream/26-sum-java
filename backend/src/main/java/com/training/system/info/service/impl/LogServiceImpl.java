package com.training.system.info.service.impl;

import com.training.system.common.PageResult;
import com.training.system.info.mapper.OperateLogMapper;
import com.training.system.info.service.LogService;
import com.training.system.info.vo.OperateLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private OperateLogMapper logMapper;

    @Override
    public PageResult<OperateLogVO> pageLogs(String keyword, String operateType, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<OperateLogVO> records = logMapper.selectPage(keyword, operateType, offset, pageSize);
        long total = logMapper.countPage(keyword, operateType);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    @Transactional
    public void clearOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        logMapper.deleteByTimeBefore(cutoff);
    }

    @Override
    public long count() {
        return logMapper.countPage(null, null);
    }
}
