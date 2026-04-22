package com.slow.excel_tools_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;

/**
 * 数据任务控制器
 */
@Api(tags = "任务管理")
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private Long getUserId(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (token.startsWith("token_")) {
            String[] parts = token.split("_");
            if (parts.length >= 2) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @ApiOperation("获取我的任务列表")
    @GetMapping
    public Result<IPage<Task>> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(taskService.listByUserId(getUserId(authHeader), page, size, keyword));
    }

    @ApiOperation("获取我的任务总数")
    @GetMapping("/count")
    public Result<Long> count(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return Result.ok(taskService.countByUserId(getUserId(authHeader)));
    }

    @ApiOperation("创建新任务")
    @PostMapping
    public Result<Task> create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody Task task) {
        task.setUserId(getUserId(authHeader));
        return Result.ok(taskService.create(task));
    }

    @ApiOperation("获取任务详情")
    @GetMapping("/{id}")
    public Result<Task> detail(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.getById(id, getUserId(authHeader)));
    }

    @ApiOperation("更新任务数据")
    @PutMapping("/{id}")
    public Result<Task> update(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id, @RequestBody Task task) {
        return Result.ok(taskService.update(id, getUserId(authHeader), task));
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id) {
        taskService.delete(id, getUserId(authHeader));
        return Result.ok();
    }

    @ApiOperation("数据统计")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.stats(id, getUserId(authHeader)));
    }

    @ApiOperation("导出Excel（单Sheet）")
    @GetMapping("/{id}/export")
    public void export(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id,
                      @ApiParam("导出文件名") @RequestParam(required = false) String fileName,
                      HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, getUserId(authHeader), fileName, response);
    }

    @ApiOperation("按列分组导出Excel（多Sheet）")
    @GetMapping("/{id}/export/group")
    public void exportGroupBy(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @ApiParam("任务ID") @PathVariable Long id,
                              @ApiParam("分组列名，如：村庄") @RequestParam String groupByField,
                              HttpServletResponse response) throws IOException {
        taskService.exportExcelGroupBy(id, getUserId(authHeader), groupByField, response);
    }
}
