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

    @TableField(value = "`columns`", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty("列定义")
    private List<ColumnDefine> columns;

    @TableField(value = "`rows`", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty("行数据")
    private List<Map<String, Object>> rows;

    @TableField("type")
    @ApiModelProperty("任务类型: parse_text, parse_excel, pdf_split, pdf_rotate, pdf_merge, pdf_encrypt, pdf_watermark, doc_convert, office_to_pdf, web_to_pdf, web_screenshot")
    private String type;

    @TableField("file_url")
    @ApiModelProperty("文件下载URL(JSON)")
    private String fileUrl;

    @TableField("source_file")
    @ApiModelProperty("源文件名")
    private String sourceFile;

    @TableField("params")
    @ApiModelProperty("任务参数JSON")
    private String params;

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
    public List<ColumnDefine> getColumns() { return columns; }
    public void setColumns(List<ColumnDefine> columns) { this.columns = columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ColumnDefine> getColumnsList() {
        return columns;
    }
    public void setColumnsList(List<ColumnDefine> list) {
        this.columns = list;
    }
    public List<Map<String, Object>> getRowsList() {
        return rows;
    }
    public void setRowsList(List<Map<String, Object>> list) {
        this.rows = list;
    }

    public String getName() { return title; }
    public Integer getRowCount() { return rows != null ? rows.size() : 0; }
    public Integer getColumnCount() { return columns != null ? columns.size() : 0; }
    public String getStatus() { return "completed"; }
}
