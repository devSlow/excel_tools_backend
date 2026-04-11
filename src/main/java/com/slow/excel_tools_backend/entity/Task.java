package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "task", autoResultMap = true)
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ColumnDefine> columns;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> rows;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
