package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 user 表
 * <p>
 * 通过微信小程序 openid 识别用户
 * </p>
 */
@Data
@TableName("user")
public class User {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信小程序 openid，唯一标识 */
    private String openid;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 URL */
    private String avatar;

    /** 创建时间，由数据库自动填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动填充 */
    private LocalDateTime updatedAt;
}
