package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.service.LogService;
import com.training.system.info.vo.OperateLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/page")
    public Result<PageResult<OperateLogVO>> page(@RequestParam(defaultValue = "") String keyword,
                                                  @RequestParam(required = false) String operateType,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(logService.pageLogs(keyword, operateType, pageNum, pageSize));
    }

    @PostMapping("/clear")
    public Result<Void> clear(@RequestParam(defaultValue = "90") int days) {
        logService.clearOldLogs(days);
        return Result.success();
    }
}
