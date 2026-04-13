package com.slow.excel_tools_backend.common;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑中可预见的错误，携带错误码和错误消息
 * </p>
 */
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(1, message);
    }

    public int getCode() { return code; }
}
