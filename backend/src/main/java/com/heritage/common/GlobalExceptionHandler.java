package com.heritage.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限异常：{}", e.getMessage());
        return Result.error(403, "无权限访问");
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<String> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常：{}", e.getMessage());
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(ClassCastException.class)
    public Result<String> handleClassCastException(ClassCastException e) {
        log.error("认证信息异常：{}", e.getMessage());
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("系统异常，请联系管理员");
    }

    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验异常：{}", message);
        return Result.error(message);
    }

    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定异常：{}", message);
        return Result.error(message);
    }
}
