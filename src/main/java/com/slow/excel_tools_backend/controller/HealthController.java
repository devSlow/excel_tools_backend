package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
public class HealthController {

    /**
     * 服务健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("ok");
    }
}
