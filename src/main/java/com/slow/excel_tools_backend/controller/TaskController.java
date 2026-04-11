package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 数据任务控制器
 */
@Api(tags = "任务管理")
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // TODO: 从请求头 token 中解析 userId，暂时硬编码
    private Long getUserId() {
        return 1L;
    }

    @ApiOperation("获取我的任务列表")
    @GetMapping
    public Result<List<Task>> list() {
        return Result.ok(taskService.listByUserId(getUserId()));
    }

    @ApiOperation("创建新任务")
    @PostMapping
    public Result<Task> create(@RequestBody Task task) {
        task.setUserId(getUserId());
        return Result.ok(taskService.create(task));
    }

    @ApiOperation("获取任务详情")
    @GetMapping("/{id}")
    public Result<Task> detail(@ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.getById(id, getUserId()));
    }

    @ApiOperation("更新任务数据")
    @PutMapping("/{id}")
    public Result<Task> update(@ApiParam("任务ID") @PathVariable Long id, @RequestBody Task task) {
        return Result.ok(taskService.update(id, getUserId(), task));
    }

    @ApiOperation("删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("任务ID") @PathVariable Long id) {
        taskService.delete(id, getUserId());
        return Result.ok();
    }

    @ApiOperation("数据统计")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@ApiParam("任务ID") @PathVariable Long id) {
        return Result.ok(taskService.stats(id, getUserId()));
    }

    @ApiOperation("导出Excel（单Sheet）")
    @GetMapping("/{id}/export")
    public void export(@ApiParam("任务ID") @PathVariable Long id, HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, getUserId(), response);
    }

    @ApiOperation("按列分组导出Excel（多Sheet）")
    @GetMapping("/{id}/export/group")
    public void exportGroupBy(@ApiParam("任务ID") @PathVariable Long id,
                              @ApiParam("分组列名，如：村庄") @RequestParam String groupByField,
                              HttpServletResponse response) throws IOException {
        taskService.exportExcelGroupBy(id, getUserId(), groupByField, response);
    }
}
