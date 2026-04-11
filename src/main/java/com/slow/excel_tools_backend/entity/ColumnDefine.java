package com.slow.excel_tools_backend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 列定义
 */
@Data
@ApiModel("列定义")
public class ColumnDefine {

    @ApiModelProperty("列名称（表头）")
    private String name;

    @ApiModelProperty("字段类型：text-文本，radio-单选，select-下拉选择")
    private String type;
}
