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

/**
 * 数据任务实体，对应 task 表
 * <p>
 * 每次文本解析或数据采集产生一个任务，包含列定义和行数据
 * </p>
 */
@Data
@TableName(value = "task", autoResultMap = true)
public class Task {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 任务标题 */
    private String title;

    /**
     * 列定义（JSON 格式）
     * <p>
     * 示例: [{"name":"姓名","type":"text"},{"name":"村庄","type":"text"}]
     * </p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ColumnDefine> columns;

    /**
     * 行数据（JSON 格式）
     * <p>
     * 每行以 key-value 形式存储，key 对应列名
     * 示例: [{"姓名":"王代宏","村庄":"黄香村"},{"姓名":"李小玉","村庄":"齐村村"}]
     * </p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> rows;

    /** 创建时间，由数据库自动填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动填充 */
    private LocalDateTime updatedAt;
}
