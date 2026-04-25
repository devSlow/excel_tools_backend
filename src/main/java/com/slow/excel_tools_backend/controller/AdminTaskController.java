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
import java.util.Map;

/**
 * 任务管理控制器（后台）
 */
@Api(tags = "任务管理（后台）")
@RestController
@RequestMapping("/api/admin/task")
public class AdminTaskController {

    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @ApiOperation("获取任务列表")
    @GetMapping
    public Result<IPage<Task>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return Result.ok(taskService.listPage(page, size, keyword, userId));
    }

    @ApiOperation("获取任务详情")
    @GetMapping("/{id}")
    public Result<Task> detail(@ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.getById(id, null));
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("任务ID") @PathVariable Long id) {
        taskService.delete(id, null);
        return Result.ok();
    }

    @ApiOperation("导出任务Excel")
    @GetMapping("/{id}/export")
    public void export(
            @ApiParam("任务ID") @PathVariable Long id,
            @ApiParam("文件名") @RequestParam(required = false) String fileName,
            HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, null, fileName, response);
    }

    @ApiOperation("数据统计")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.stats(id, null));
    }
}