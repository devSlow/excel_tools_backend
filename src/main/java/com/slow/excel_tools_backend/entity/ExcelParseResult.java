package com.slow.excel_tools_backend.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Excel 文件解析结果（多 Sheet）
 */
@Data
@ApiModel("Excel解析结果")
public class ExcelParseResult {

    @ApiModelProperty("源文件名")
    private String fileName;

    @ApiModelProperty("各Sheet解析结果")
    private List<SheetData> sheets;
}
