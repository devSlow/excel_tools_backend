package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation("微信小程序登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            throw new BusinessException(3001, "登录code不能为空");
        }

        // TODO: 调用微信 API 用 code 换取 openid，暂时用 code 作为 openid
        String openid = code;

        User user = userService.loginOrRegister(openid);

        String token = "token_" + user.getId() + "_" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("userInfo", Map.of("id", user.getId(), "openid", user.getOpenid()));
        return Result.ok(data);
    }
}
