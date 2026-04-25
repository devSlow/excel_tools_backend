package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台认证控制器
 */
@Api(tags = "管理后台认证")
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @ApiOperation("管理后台登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return Result.fail(400, "用户名或密码不能为空");
        }

        // 简单的管理员验证，可以后续从数据库配置
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = "admin_token_" + System.currentTimeMillis();
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            return Result.ok(data);
        }

        return Result.fail(401, "用户名或密码错误");
    }
}