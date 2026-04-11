package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.ExcelParseResult;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.ExcelParseService;
import com.slow.excel_tools_backend.service.ParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 数据解析控制器
 * <p>
 * 提供文本和 Excel 文件的智能解析功能，将非结构化数据转为结构化数据
 * </p>
 */
@RestController
@RequestMapping("/api/parse")
@RequiredArgsConstructor
public class ParseController {

    private final ParseService parseService;
    private final ExcelParseService excelParseService;

    /**
     * 文本解析接口
     * <p>
     * 自动识别分隔符（空格、Tab、逗号、换行），将文本拆分为列定义和行数据
     * </p>
     *
     * @param body 包含 text 字段的请求体
     * @return 解析后的任务结构（columns + rows）
     */
    @PostMapping("/text")
    public Result<Task> parseText(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        Task task = parseService.parseText(text);
        return Result.ok(task);
    }

    /**
     * Excel 文件导入解析接口（支持多 Sheet）
     * <p>
     * 上传 .xlsx/.xls 文件，遍历所有 Sheet，返回每个 Sheet 的 sheetName + columns + rows。
     * 文件同时备份至 MinIO 存储。
     * </p>
     *
     * @param file 上传的 Excel 文件（multipart/form-data）
     * @return 多 Sheet 解析结果
     */
    @PostMapping("/excel")
    public Result<ExcelParseResult> parseExcel(MultipartFile file) {
        ExcelParseResult result = excelParseService.parseExcel(file);
        return Result.ok(result);
    }
}
