package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("redeem_code")
public class RedeemCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plainCode;

    private String code;

    private String salt;

    private Integer maxUsage;

    private Integer usedCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}