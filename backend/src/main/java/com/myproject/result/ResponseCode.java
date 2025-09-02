package com.myproject.result;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或token失效"),
    FORBIDDEN(403, "没有权限");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

