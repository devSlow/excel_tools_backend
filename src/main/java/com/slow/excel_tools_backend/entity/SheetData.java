package com.slow.excel_tools_backend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 单个 Sheet 的解析结果
 */
@ApiModel("Sheet数据")
public class SheetData {

    @ApiModelProperty("Sheet名称")
    private String sheetName;

    @ApiModelProperty("列定义")
    private List<ColumnDefine> columns;

    @ApiModelProperty("行数据")
    private List<Map<String, Object>> rows;

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public List<ColumnDefine> getColumns() { return columns; }
    public void setColumns(List<ColumnDefine> columns) { this.columns = columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}
