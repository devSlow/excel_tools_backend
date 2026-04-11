package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据任务实体
 */
@Data
@TableName(value = "task", autoResultMap = true)
@ApiModel("数据任务")
public class Task {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("任务ID")
    private Long id;

    @ApiModelProperty("所属用户ID")
    private Long userId;

    @ApiModelProperty("任务标题")
    private String title;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty("列定义，如：[{\"name\":\"姓名\",\"type\":\"text\"}]")
    private List<ColumnDefine> columns;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty("行数据，如：[{\"姓名\":\"王代宏\",\"村庄\":\"黄香村\"}]")
    private List<Map<String, Object>> rows;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
