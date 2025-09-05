package com.myproject.exception;

import com.myproject.result.ResponseResult;
import com.myproject.result.ResponseCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<?> handleBusinessException(BusinessException ex) {
        return ResponseResult.fail(ex.getCode(), ex.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseResult.fail(ResponseCode.VALIDATE_FAILED.getCode(), errorMessage);
    }

    // 其他未捕获异常
    @ExceptionHandler(Exception.class)
    public ResponseResult<?> handleException(Exception ex) {
        ex.printStackTrace(); // 方便调试，生产环境可关闭
        return ResponseResult.fail(ResponseCode.FAIL.getCode(), "服务器内部错误" + ex.getMessage());
    }
}
