package com.slow.excel_tools_backend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 单个 Sheet 的解析结果
 */
@Data
@ApiModel("Sheet数据")
public class SheetData {

    @ApiModelProperty("Sheet名称")
    private String sheetName;

    @ApiModelProperty("列定义")
    private List<ColumnDefine> columns;

    @ApiModelProperty("行数据")
    private List<Map<String, Object>> rows;
}
