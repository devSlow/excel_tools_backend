package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据任务实体
 */
@TableName(value = "task", autoResultMap = true)
@ApiModel("数据任务")
public class Task {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("任务ID")
    private Long id;

    @TableField("user_id")
    @ApiModelProperty("所属用户ID")
    private Long userId;

    @TableField("title")
    @ApiModelProperty("任务标题")
    private String title;

    @TableField("`columns`")
    @ApiModelProperty("列定义")
    private String columns;

    @TableField("`rows`")
    @ApiModelProperty("行数据")
    private String rows;

    @TableField("created_at")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getColumns() { return columns; }
    public void setColumns(String columns) { this.columns = columns; }
    public String getRows() { return rows; }
    public void setRows(String rows) { this.rows = rows; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ColumnDefine> getColumnsList() {
        System.out.println("=== getColumnsList START ===");
        System.out.println("columns raw: [" + columns + "]");
        if (columns == null) {
            System.out.println("columns is null, returning null");
            return null;
        }
        try {
            String json = columns.trim();
            System.out.println("json to parse: [" + json + "]");
            // 检查是否需要处理转义
            if (json.startsWith("\"") && json.endsWith("\"")) {
                // 去掉外层引号，并处理转义
                json = json.substring(1, json.length() - 1).replace("\\\"", "\"");
            }
            System.out.println("final json: [" + json + "]");
            List<ColumnDefine> result = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, 
                new com.fasterxml.jackson.core.type.TypeReference<List<ColumnDefine>>() {});
            System.out.println("parsed successfully, size: " + result.size());
            return result;
        } catch (Exception e) { 
            System.out.println("getColumnsList error: " + e.getMessage());
            e.printStackTrace();
            return null; 
        }
    }
    public void setColumnsList(List<ColumnDefine> list) {
        try {
            this.columns = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
            System.out.println("setColumnsList called, columns=" + this.columns);
        } catch (Exception e) {}
    }
    public List<Map<String, Object>> getRowsList() {
        System.out.println("=== getRowsList START ===");
        System.out.println("rows raw: [" + rows + "]");
        if (rows == null) {
            System.out.println("rows is null, returning null");
            return null;
        }
        try {
            String json = rows.trim();
            System.out.println("json to parse: [" + json + "]");
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1).replace("\\\"", "\"");
            }
            System.out.println("final json: [" + json + "]");
            List<Map<String, Object>> result = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, 
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            System.out.println("parsed successfully, size: " + result.size());
            return result;
        } catch (Exception e) { 
            System.out.println("getRowsList error: " + e.getMessage());
            e.printStackTrace();
            return null; 
        }
    }
    public void setRowsList(List<Map<String, Object>> list) {
        try {
            this.rows = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
            System.out.println("setRowsList called, rows=" + this.rows);
        } catch (Exception e) {}
    }
    
    // For JSON deserialization from request body
    public void setColumns(List<ColumnDefine> list) { setColumnsList(list); }
    public void setRows(List<Map<String, Object>> list) { setRowsList(list); }

    // 前端显示用属性
    public String getName() { return title; }
    public Integer getRowCount() { return getRowsList() != null ? getRowsList().size() : 0; }
    public Integer getColumnCount() { return getColumnsList() != null ? getColumnsList().size() : 0; }
    public String getStatus() { return "completed"; }
}
