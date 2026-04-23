package com.slow.excel_tools_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 轮播图实体
 */
@TableName(value = "banner", autoResultMap = true)
@ApiModel("轮播图")
public class Banner {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("轮播图ID")
    private Long id;

    @TableField("title")
    @ApiModelProperty("标题")
    private String title;

    @TableField("image_url")
    @ApiModelProperty("图片地址")
    private String imageUrl;

    @TableField("link_url")
    @ApiModelProperty("跳转链接")
    private String linkUrl;

    @TableField("content")
    @ApiModelProperty("公告内容，HTML富文本")
    private String content;

    @TableField("type")
    @ApiModelProperty("类型：info-一般通知/warning-警告/error-紧急")
    private String type;

    @TableField("sort_order")
    @ApiModelProperty("排序顺序")
    private Integer sortOrder;

    @TableField("status")
    @ApiModelProperty("状态 1:启用 0:禁用")
    private Integer status;

    @TableField("created_at")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @ApiModelProperty("是否启用")
    public boolean getEnabled() { return status != null && status == 1; }
}