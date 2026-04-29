package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

@TableName(value = "app_config")
@ApiModel("应用配置")
public class AppConfig {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("ID")
    private Long id;

    @TableField("config_key")
    @ApiModelProperty("配置键")
    private String configKey;

    @TableField("config_value")
    @ApiModelProperty("配置值")
    private String configValue;

    @TableField("update_content")
    @ApiModelProperty("更新公告内容")
    private String updateContent;

    @TableField("update_image")
    @ApiModelProperty("更新公告插图")
    private String updateImage;

    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @TableField("created_at")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getUpdateContent() { return updateContent; }
    public void setUpdateContent(String updateContent) { this.updateContent = updateContent; }
    public String getUpdateImage() { return updateImage; }
    public void setUpdateImage(String updateImage) { this.updateImage = updateImage; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
