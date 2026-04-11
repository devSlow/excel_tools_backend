package com.slow.excel_tools_backend.common;

import lombok.Data;

/**
 * 统一响应封装
 * <p>
 * code=0 表示成功，非0表示失败
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** 响应码，0-成功，非0-失败 */
    private int code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    private Result() {}

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     */
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（默认错误码1）
     *
     * @param msg 错误消息
     */
    public static <T> Result<T> fail(String msg) {
        return fail(1, msg);
    }

    /**
     * 失败响应（自定义错误码）
     *
     * @param code 错误码
     * @param msg  错误消息
     */
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
