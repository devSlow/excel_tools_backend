package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.service.BannerService;
import com.slow.excel_tools_backend.service.TaskService;
import com.slow.excel_tools_backend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器（后台）
 */
@Api(tags = "数据统计（后台）")
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserService userService;
    private final TaskService taskService;
    private final BannerService bannerService;

    public AdminStatsController(UserService userService, TaskService taskService, BannerService bannerService) {
        this.userService = userService;
        this.taskService = taskService;
        this.bannerService = bannerService;
    }

    @ApiOperation("获取统计概览")
    @GetMapping
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userService.countAll());
        data.put("totalTasks", taskService.countAll());
        data.put("todayTasks", taskService.countToday());
        data.put("totalBanners", bannerService.countAll());
        return Result.ok(data);
    }
}