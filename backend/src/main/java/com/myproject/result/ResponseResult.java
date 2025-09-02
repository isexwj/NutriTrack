package com.myproject.result;

import lombok.Data;

@Data
public class ResponseResult<T> {

    private Integer code;   // 状态码
    private String message; // 提示信息
    private T data;         // 返回数据

    private ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功返回
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    // 成功返回（无数据）
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), null);
    }

    // 失败返回
    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    // 失败返回（默认错误码）
    public static <T> ResponseResult<T> fail(String message) {
        return new ResponseResult<>(ResponseCode.FAIL.getCode(), message, null);
    }
}
