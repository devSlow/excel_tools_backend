package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.AppConfig;
import com.slow.excel_tools_backend.service.AppConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "应用配置管理（后台）")
@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    private final AppConfigService appConfigService;

    public AdminConfigController(AppConfigService appConfigService) {
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

    @ApiOperation("更新小程序版本信息")
    @PutMapping("/version")
    public Result<Void> updateVersion(@RequestBody Map<String, String> body) {
        String version = body.get("version");
        String updateContent = body.get("updateContent");
        String updateImage = body.get("updateImage");
        if (version == null || version.isEmpty()) {
            return Result.fail("版本号不能为空");
        }
        appConfigService.setValue("app_version", version, updateContent, updateImage, "小程序版本号");
        return Result.ok();
    }
}
