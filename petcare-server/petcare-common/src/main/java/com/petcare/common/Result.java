package com.petcare.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未授权，请登录", null);
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(403, "无权限访问", null);
    }

    public static <T> Result<T> notFound() {
        return new Result<>(404, "资源不存在", null);
    }

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }
}