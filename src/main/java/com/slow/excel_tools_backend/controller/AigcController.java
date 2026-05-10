package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.AigcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/aigc")
public class AigcController {

    @Autowired
    private AigcService aigcService;

    @PostMapping("/optimize")
    public Result optimize(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("请输入需要优化的内容");
        }

        String apiUrl = request.get("apiUrl");
        String apiKey = request.get("apiKey");
        String modelName = request.get("modelName");

        Map<String, Object> result = aigcService.optimizeArticle(content, apiUrl, apiKey, modelName);

        if ((boolean) result.getOrDefault("success", false)) {
            return Result.ok(result);
        } else {
            return Result.fail((String) result.get("text"));
        }
    }
}