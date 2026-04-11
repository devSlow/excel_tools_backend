package com.slow.excel_tools_backend.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 统一响应封装
 */
@Data
@ApiModel("统一响应结果")
public class Result<T> {

    @ApiModelProperty("响应码，0-成功，非0-失败")
    private int code;

    @ApiModelProperty("响应消息")
    private String msg;

    @ApiModelProperty("响应数据")
    private T data;

    private Result() {}

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(1, msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
