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

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // TODO: 从请求头 token 中解析 userId，暂时通过请求参数传递
    private Long getUserId() {
        return 1L;
    }

    @GetMapping
    public Result<List<Task>> list() {
        return Result.ok(taskService.listByUserId(getUserId()));
    }

    @PostMapping
    public Result<Task> create(@RequestBody Task task) {
        task.setUserId(getUserId());
        return Result.ok(taskService.create(task));
    }

    @GetMapping("/{id}")
    public Result<Task> detail(@PathVariable Long id) {
        return Result.ok(taskService.getById(id, getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        return Result.ok(taskService.update(id, getUserId(), task));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id, getUserId());
        return Result.ok();
    }

    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.ok(taskService.stats(id, getUserId()));
    }

    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, HttpServletResponse response) throws IOException {
        taskService.exportExcel(id, getUserId(), response);
    }
}
