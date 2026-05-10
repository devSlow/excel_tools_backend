package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.RedeemCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/redeem")
public class RedeemCodeController {

    @Autowired
    private RedeemCodeService redeemCodeService;

    @PostMapping("/generate")
    public Result generate(@RequestParam(defaultValue = "1") int count,
                          @RequestParam(defaultValue = "5") int maxUsage,
                          @RequestParam(required = false) Integer expireDays) {
        if (count <= 0 || count > 100) {
            return Result.fail("生成数量必须在1-100之间");
        }
        if (maxUsage <= 0) {
            return Result.fail("使用次数必须大于0");
        }

        Map<String, Object> result = redeemCodeService.generateCodes(count, maxUsage, expireDays);
        return Result.ok(result);
    }

    @PostMapping("/verify")
    public Result verify(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return Result.fail("请输入兑换码");
        }

        Map<String, Object> result = redeemCodeService.verifyCode(code.trim());
        boolean valid = (boolean) result.get("valid");

        if (valid) {
            return Result.ok(result);
        } else {
            return Result.fail((String) result.get("message"));
        }
    }

    @GetMapping("/check")
    public Result check(@RequestParam String code) {
        Integer remaining = redeemCodeService.getRemainingUsage(code);
        if (remaining > 0) {
            return Result.ok(Map.of("remaining", remaining, "valid", true));
        }
        return Result.ok(Map.of("remaining", 0, "valid", false));
    }

    @GetMapping("/available")
    public Result getAvailable() {
        Map<String, Object> result = redeemCodeService.getAvailableCode();
        if ((boolean) result.getOrDefault("hasCode", false)) {
            return Result.ok(result);
        }
        return Result.fail((String) result.getOrDefault("message", "暂无可用兑换码"));
    }
}