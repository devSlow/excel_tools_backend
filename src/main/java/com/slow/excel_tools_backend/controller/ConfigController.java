package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.AppConfig;
import com.slow.excel_tools_backend.service.AppConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "应用配置（公开）")
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final AppConfigService appConfigService;

    public ConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @ApiOperation("获取小程序版本信息")
    @GetMapping("/version")
    public Result<Map<String, String>> getVersion() {
        AppConfig config = appConfigService.getByKey("app_version");
        Map<String, String> data = new HashMap<>();
        if (config != null) {
            data.put("version", config.getConfigValue() != null ? config.getConfigValue() : "1.0.0");
            data.put("updateContent", config.getUpdateContent() != null ? config.getUpdateContent() : "");
            data.put("updateImage", config.getUpdateImage() != null ? config.getUpdateImage() : "");
        } else {
            data.put("version", "1.0.0");
            data.put("updateContent", "");
            data.put("updateImage", "");
        }
        return Result.ok(data);
    }
}
