package com.training.system.exception;

import com.training.system.common.Result;
import com.training.system.common.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.fail(ex.getResultCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequest(Exception ex) {
        LOGGER.warn("请求参数不合法: {}", ex.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, "请求参数不合法，请检查输入内容");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException ex) {
        LOGGER.warn("数据重复: {}", ex.getMessage());
        String msg = parseDuplicateMessage(ex);
        return Result.fail(ResultCode.CONFLICT, msg);
    }

    @ExceptionHandler(SQLException.class)
    public Result<Void> handleSQLException(SQLException ex) {
        LOGGER.error("数据库异常", ex);
        return Result.fail(ResultCode.ERROR, "数据库操作失败，请联系管理员");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        LOGGER.error("系统异常", ex);
        return Result.fail(ResultCode.ERROR, "系统异常，请联系管理员");
    }

    /**
     * 将 MySQL 的 Duplicate entry 错误翻译为中文提示
     */
    private String parseDuplicateMessage(DuplicateKeyException ex) {
        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        if (msg == null) return "数据已存在，请勿重复提交";

        // Duplicate entry 'xxx' for key 'table.column'
        if (msg.contains("username")) {
            return "该用户名已存在，请更换学号或工号";
        }
        if (msg.contains("student_no")) {
            return "该学号已存在，请检查后重试";
        }
        if (msg.contains("teacher_no")) {
            return "该工号已存在，请检查后重试";
        }
        if (msg.contains("phone")) {
            return "该手机号已被使用，请更换后重试";
        }
        if (msg.contains("email")) {
            return "该邮箱已被使用，请更换后重试";
        }
        return "数据已存在，请勿重复提交";
    }
}
