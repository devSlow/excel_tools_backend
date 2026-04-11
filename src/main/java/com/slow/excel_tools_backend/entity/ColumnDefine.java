package com.slow.excel_tools_backend.entity;

import lombok.Data;

/**
 * 列定义
 * <p>
 * 描述表格中每一列的名称和字段类型
 * </p>
 */
@Data
public class ColumnDefine {

    /** 列名称（即表头名称） */
    private String name;

    /** 字段类型：text-文本，radio-单选，select-下拉选择 */
    private String type;
}
