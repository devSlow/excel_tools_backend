package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
@ApiModel("用户")
public class User {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("微信openid")
    private String openid;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
