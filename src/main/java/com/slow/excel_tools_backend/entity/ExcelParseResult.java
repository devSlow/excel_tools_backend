package com.slow.excel_tools_backend.entity;

import lombok.Data;

import java.util.List;

/**
 * Excel 文件解析结果（多 Sheet）
 */
@Data
public class ExcelParseResult {

    /** 源文件名 */
    private String fileName;

    /** 各 Sheet 解析结果 */
    private List<SheetData> sheets;
}
