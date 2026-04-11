package com.slow.excel_tools_backend.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 单个 Sheet 的解析结果
 */
@Data
public class SheetData {

    /** Sheet 名称 */
    private String sheetName;

    /** 列定义 */
    private List<ColumnDefine> columns;

    /** 行数据 */
    private List<Map<String, Object>> rows;
}
