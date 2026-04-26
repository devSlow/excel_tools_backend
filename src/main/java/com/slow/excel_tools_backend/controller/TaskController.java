package com.slow.excel_tools_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Long getUserIdSafe(HttpServletRequest request) {
        try {
            return getUserId(request);
        } catch (Exception e) {
            return null;
        }
    }

    @ApiOperation("获取我的任务列表")
    @GetMapping
    public Result<IPage<Task>> list(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(taskService.listByUserId(getUserId(request), page, size, keyword));
    }

    @ApiOperation("获取我的任务总数")
    @GetMapping("/count")
    public Result<Long> count(HttpServletRequest request) {
        return Result.ok(taskService.countByUserId(getUserId(request)));
    }

    @ApiOperation("获取我的数据行总数")
    @GetMapping("/count/rows")
    public Result<Long> countRows(HttpServletRequest request) {
        return Result.ok(taskService.countRowsByUserId(getUserId(request)));
    }

    @ApiOperation("创建新任务")
    @PostMapping
    public Result<Task> create(HttpServletRequest request, @RequestBody Task task) {
        task.setUserId(getUserId(request));
        return Result.ok(taskService.create(task));
    }

    @ApiOperation("获取任务详情")
    @GetMapping("/{id}")
    public Result<Task> detail(
            HttpServletRequest request,
            @ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.getById(id, getUserId(request)));
    }

    @ApiOperation("更新任务数据")
    @PutMapping("/{id}")
    public Result<Task> update(
            HttpServletRequest request,
            @ApiParam("任务ID") @PathVariable Long id, @RequestBody Task task) {
        return Result.ok(taskService.update(id, getUserId(request), task));
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            HttpServletRequest request,
            @ApiParam("任务ID") @PathVariable Long id) {
        taskService.delete(id, getUserId(request));
        return Result.ok();
    }

    @ApiOperation("数据统计")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(
            HttpServletRequest request,
            @ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.stats(id, getUserId(request)));
    }

    @ApiOperation("导出Excel（单Sheet）")
    @GetMapping("/{id}/export")
    public void export(
            @ApiParam("任务ID") @PathVariable Long id,
            @ApiParam("导出文件名") @RequestParam(required = false) String fileName,
            HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, null, fileName, response);
    }

    @ApiOperation("按列分组导出Excel（多Sheet）")
    @GetMapping("/{id}/export/group")
    public void exportGroupBy(
            HttpServletRequest request,
            @ApiParam("任务ID") @PathVariable Long id,
            @ApiParam("分组列名，如：村庄") @RequestParam String groupByField,
            HttpServletResponse response) throws IOException {
        taskService.exportExcelGroupBy(id, getUserId(request), groupByField, response);
    }
}
