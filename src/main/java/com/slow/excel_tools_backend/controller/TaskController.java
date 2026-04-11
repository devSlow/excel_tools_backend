package com.slow.excel_tools_backend.controller;

import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 数据任务控制器
 * <p>
 * 提供任务的增删改查、数据统计、Excel 导出功能
 * </p>
 */
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // TODO: 从请求头 token 中解析 userId，暂时硬编码
    private Long getUserId() {
        return 1L;
    }

    /**
     * 获取当前用户的任务列表
     */
    @GetMapping
    public Result<List<Task>> list() {
        return Result.ok(taskService.listByUserId(getUserId()));
    }

    /**
     * 创建新任务
     *
     * @param task 任务数据（包含 columns、rows）
     */
    @PostMapping
    public Result<Task> create(@RequestBody Task task) {
        task.setUserId(getUserId());
        return Result.ok(taskService.create(task));
    }

    /**
     * 获取任务详情（含完整行列数据）
     *
     * @param id 任务ID
     */
    @GetMapping("/{id}")
    public Result<Task> detail(@PathVariable Long id) {
        return Result.ok(taskService.getById(id, getUserId()));
    }

    /**
     * 更新任务数据（行列内容）
     *
     * @param id   任务ID
     * @param task 更新后的任务数据
     */
    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        return Result.ok(taskService.update(id, getUserId(), task));
    }

    /**
     * 删除任务
     *
     * @param id 任务ID
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id, getUserId());
        return Result.ok();
    }

    /**
     * 数据统计
     * <p>
     * 返回总行数和每列按值的分类统计
     * </p>
     *
     * @param id 任务ID
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.ok(taskService.stats(id, getUserId()));
    }

    /**
     * 导出 Excel 文件
     * <p>
     * 根据任务的列定义和行数据生成 .xlsx 文件并下载
     * </p>
     *
     * @param id       任务ID
     * @param response HTTP 响应（直接写入文件流）
     */
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, getUserId(), response);
    }
}
