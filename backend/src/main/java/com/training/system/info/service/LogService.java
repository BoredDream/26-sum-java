package com.training.system.info.service;

import com.training.system.common.PageResult;
import com.training.system.info.vo.OperateLogVO;

/**
 * 操作日志服务接口
 */
public interface LogService {

    PageResult<OperateLogVO> pageLogs(String keyword, String operateType, int pageNum, int pageSize);

    void clearOldLogs(int days);

    long count();
}
