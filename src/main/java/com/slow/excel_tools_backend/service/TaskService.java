package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.Task;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TaskService {

    List<Task> listByUserId(Long userId);

    Task getById(Long id, Long userId);

    Task create(Task task);

    Task update(Long id, Long userId, Task update);

    void delete(Long id, Long userId);

    Map<String, Object> stats(Long id, Long userId);

    void exportExcel(Long id, Long userId, HttpServletResponse response) throws IOException;
}
