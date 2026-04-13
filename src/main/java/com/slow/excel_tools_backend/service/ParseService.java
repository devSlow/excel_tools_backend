package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.Task;

/**
 * 文本解析服务接口
 */
public interface ParseService {

    /**
     * 文本智能分列解析
     * <p>
     * 自动识别分隔符（Tab、逗号、多空格、单空格），将文本拆分为列定义和行数据。
     * 单列数据默认列名为"姓名"，多列数据取第一行作为表头。
     * </p>
     *
     * @param text      原始文本内容
     * @param delimiter 分隔符（为空时自动识别）
     * @return 解析后的任务结构（columns + rows）
     */
    Task parseText(String text, String delimiter);
}
