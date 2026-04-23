package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.common.JwtUtil;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.config.WechatConfig;
import com.slow.excel_tools_backend.entity.User;
import com.slow.excel_tools_backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final WechatConfig wechatConfig;

    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    public AuthController(UserService userService, WechatConfig wechatConfig) {
        this.userService = userService;
        this.wechatConfig = wechatConfig;
    }

    @ApiOperation("微信小程序登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            throw new BusinessException(3001, "登录code不能为空");
        }

        // 调用微信 API 用 code 换取 openid
        String url = String.format(JSCODE2SESSION_URL,
                wechatConfig.getAppid(), wechatConfig.getSecret(), code);

        RestTemplate restTemplate = new RestTemplate();
        @SuppressWarnings("unchecked")
        Map<String, Object> wxResult = restTemplate.getForObject(url, Map.class);

        if (wxResult == null || wxResult.get("openid") == null) {
            log.error("微信登录失败，微信返回: {}", wxResult);
            throw new BusinessException(3002, "微信登录失败");
        }

        String openid = (String) wxResult.get("openid");

        User user = userService.loginOrRegister(openid);

        String token = JwtUtil.generate(user.getId(), user.getOpenid());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("userInfo", Map.of("id", user.getId(), "openid", user.getOpenid()));
        return Result.ok(data);
    }
}
