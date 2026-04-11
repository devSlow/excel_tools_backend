package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.ExcelParseResult;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.ExcelParseService;
import com.slow.excel_tools_backend.service.ParseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 数据解析控制器
 */
@Api(tags = "数据解析")
@RestController
@RequestMapping("/api/parse")
@RequiredArgsConstructor
public class ParseController {

    private final ParseService parseService;
    private final ExcelParseService excelParseService;

    @ApiOperation("文本智能解析")
    @PostMapping("/text")
    public Result<Task> parseText(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        Task task = parseService.parseText(text);
        return Result.ok(task);
    }

    @ApiOperation("Excel文件导入解析（支持多Sheet）")
    @PostMapping("/excel")
    public Result<ExcelParseResult> parseExcel(MultipartFile file) {
        ExcelParseResult result = excelParseService.parseExcel(file);
        return Result.ok(result);
    }
}
