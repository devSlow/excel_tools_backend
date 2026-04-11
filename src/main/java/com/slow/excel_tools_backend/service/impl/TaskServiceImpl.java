package com.slow.excel_tools_backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.mapper.TaskMapper;
import com.slow.excel_tools_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据任务服务实现类
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    @Override
    public List<Task> listByUserId(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
               .orderByDesc(Task::getCreatedAt);
        return taskMapper.selectList(wrapper);
    }

    @Override
    public Task getById(Long id, Long userId) {
        Task task = taskMapper.selectById(id);
        // 校验任务是否存在且属于当前用户
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("任务不存在或无权访问");
        }
        return task;
    }

    @Override
    public Task create(Task task) {
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task update(Long id, Long userId, Task update) {
        Task existing = getById(id, userId);
        existing.setTitle(update.getTitle());
        existing.setColumns(update.getColumns());
        existing.setRows(update.getRows());
        taskMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id, Long userId) {
        Task existing = getById(id, userId);
        taskMapper.deleteById(existing.getId());
    }

    @Override
    public Map<String, Object> stats(Long id, Long userId) {
        Task task = getById(id, userId);
        Map<String, Object> result = new LinkedHashMap<>();
        // 统计总行数
        result.put("total", task.getRows() != null ? task.getRows().size() : 0);

        // 按列统计各值的数量
        if (task.getColumns() != null && task.getRows() != null) {
            for (ColumnDefine col : task.getColumns()) {
                Map<String, Integer> countMap = new LinkedHashMap<>();
                for (Map<String, Object> row : task.getRows()) {
                    Object val = row.get(col.getName());
                    String key = val != null ? val.toString() : "空";
                    countMap.merge(key, 1, Integer::sum);
                }
                result.put(col.getName(), countMap);
            }
        }
        return result;
    }

    @Override
    public void exportExcel(Long id, Long userId, HttpServletResponse response) throws IOException {
        Task task = getById(id, userId);

        // 设置响应头
        String fileName = (task.getTitle() != null ? task.getTitle() : "export") + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 提取列名作为表头
        List<String> headers = new ArrayList<>();
        if (task.getColumns() != null) {
            for (ColumnDefine col : task.getColumns()) {
                headers.add(col.getName());
            }
        }

        // 构建行数据，按列顺序填充
        List<List<Object>> dataList = new ArrayList<>();
        if (task.getRows() != null) {
            for (Map<String, Object> row : task.getRows()) {
                List<Object> rowData = new ArrayList<>();
                for (String header : headers) {
                    rowData.add(row.getOrDefault(header, ""));
                }
                dataList.add(rowData);
            }
        }

        // 使用 EasyExcel 写入响应流
        EasyExcel.write(response.getOutputStream())
                .head(headers.stream().map(h -> {
                    List<String> head = new ArrayList<>();
                    head.add(h);
                    return head;
                }).collect(Collectors.toList()))
                .sheet("数据")
                .doWrite(dataList);
    }
}
