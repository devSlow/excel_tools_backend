package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.ParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 文本解析控制器
 * <p>
 * 提供文本智能分列解析功能，将非结构化文本转为结构化数据
 * </p>
 */
@RestController
@RequestMapping("/api/parse")
@RequiredArgsConstructor
public class ParseController {

    private final ParseService parseService;

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
}
