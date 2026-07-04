package com.training.system.info.aspect;

import com.training.system.info.annotation.OperationLog;
import com.training.system.info.entity.OperateLog;
import com.training.system.info.mapper.OperateLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperateLogMapper operateLogMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String status = "成功";
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "失败: " + e.getMessage();
            throw e;
        } finally {
            try {
                saveLog(joinPoint, operationLog, status);
            } catch (Exception ex) {
                log.error("保存操作日志失败", ex);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, String status) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpSession session = attrs.getRequest().getSession();
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) userId = 0L;

        String description = operationLog.description();
        if (description.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            description = signature.getMethod().getName();
        }

        OperateLog logEntry = new OperateLog();
        logEntry.setOperateUserId(userId);
        logEntry.setOperateType(operationLog.type());
        logEntry.setOperateContent(description + " - " + status);
        logEntry.setOperateTime(LocalDateTime.now());
        operateLogMapper.insert(logEntry);
    }
}
