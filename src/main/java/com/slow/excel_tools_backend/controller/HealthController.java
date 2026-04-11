package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@Api(tags = "系统监控")
@RestController
public class HealthController {

    @ApiOperation("服务健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("ok");
    }
}
