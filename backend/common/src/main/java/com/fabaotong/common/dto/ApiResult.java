package com.fabaotong.common.dto;

import lombok.Data;

@Data
public class ApiResult<T> {
    private int code;
    private String msg;
    private T data;

    private ApiResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(0, "成功", data);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(0, "成功", null);
    }

    public static <T> ApiResult<T> error(int code, String msg) {
        return new ApiResult<>(code, msg, null);
    }

    public static <T> ApiResult<T> error(String msg) {
        return new ApiResult<>(-1, msg, null);
    }
}
