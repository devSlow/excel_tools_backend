package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 处理微信小程序登录认证
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 微信小程序登录
     * <p>
     * 接收前端传入的微信 code，换取 openid 完成登录或自动注册
     * </p>
     *
     * @param body 包含 code 字段的请求体
     * @return 用户信息（userId、openid）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            throw new BusinessException(3001, "登录code不能为空");
        }

        // TODO: 调用微信 API 用 code 换取 openid，暂时用 code 作为 openid
        String openid = code;

        User user = userService.loginOrRegister(openid);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("openid", user.getOpenid());
        return Result.ok(data);
    }
}
