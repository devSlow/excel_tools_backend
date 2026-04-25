package com.slow.excel_tools_backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.common.ExcelStyleUtil;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.mapper.TaskMapper;
import com.slow.excel_tools_backend.service.TaskService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

@Override
    public IPage<Task> listByUserId(Long userId, int page, int size, String keyword) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
               .orderByDesc(Task::getCreatedAt);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Task::getTitle, keyword.trim())
                          .or()
                          .like(Task::getColumns, keyword.trim())
                          .or()
                          .like(Task::getRows, keyword.trim()));
        }
        
        IPage<Task> result = taskMapper.selectPage(new Page<>(page, size), wrapper);

        return result;
    }

@Override
    public long countByUserId(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        return taskMapper.selectCount(wrapper);
    }

    @Override
    public Task getById(Long id, Long userId) {
        Task task = taskMapper.selectById(id);

        // 校验任务是否存在
        if (task == null) {
            throw new BusinessException(2001, "任务不存在");
        }
        // 校验任务是否属于当前用户
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException(2002, "无权访问该任务");
        }
        return task;
    }

    @Override
    public Task create(Task task) {
        // 兼容前端传 name 和 data 的情况
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            task.setTitle(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (task.getRows() == null && task.getColumns() != null) {
            // 前端可能把数据放在 columns 里，需要从 Task 实体的其他字段处理
        }
        
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).toLocalDateTime());
        }
        if (task.getUpdatedAt() == null) {
            task.setUpdatedAt(java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).toLocalDateTime());
        }
        taskMapper.insert(task);

        return task;
    }

    @Override
    public Task update(Long id, Long userId, Task update) {
        Task existing = getById(id, userId);
        existing.setTitle(update.getTitle());
        existing.setColumns(update.getColumns());
        existing.setRows(update.getRows());
        existing.setUpdatedAt(java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai")).toLocalDateTime());
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
        
        Object rowsObj = task.getRows();
        Object colsObj = task.getColumns();
        List<Map<String, Object>> rowsList = rowsObj instanceof List ? (List<Map<String, Object>>) rowsObj : null;
        List<String> columnNames = new ArrayList<>();
        
        if (colsObj instanceof List) {
            for (Object item : (List<?>) colsObj) {
                if (item instanceof ColumnDefine) {
                    columnNames.add(((ColumnDefine) item).getName());
                } else if (item instanceof Map) {
                    columnNames.add((String) ((Map<?, ?>) item).get("name"));
                }
            }
        }
        
        // 统计总行数
        result.put("total", rowsList != null ? rowsList.size() : 0);

        // 按列统计各值的数量
        if (!columnNames.isEmpty() && rowsList != null) {
            for (String colName : columnNames) {
                Map<String, Integer> countMap = new LinkedHashMap<>();
                for (Map<String, Object> row : rowsList) {
                    Object val = row.get(colName);
                    String key = val != null ? val.toString() : "空";
                    countMap.merge(key, 1, Integer::sum);
                }
                result.put(colName, countMap);
            }
        }
        return result;
    }

    @Override
    public void exportExcel(Long id, Long userId, String fileName, HttpServletResponse response) throws IOException {
        Task task = getById(id, userId);

        String exportFileName = (fileName != null && !fileName.trim().isEmpty()) 
            ? fileName 
            : (task.getTitle() != null ? task.getTitle() : "export");
        exportFileName += ".xlsx";
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(exportFileName, "UTF-8"));

        // 提取列名作为表头
        List<String> headers = new ArrayList<>();
        Object columnsObj = task.getColumns();
        if (columnsObj instanceof List) {
            for (Object item : (List<?>) columnsObj) {
                if (item instanceof ColumnDefine) {
                    headers.add(((ColumnDefine) item).getName());
                } else if (item instanceof Map) {
                    headers.add((String) ((Map<?, ?>) item).get("name"));
                }
            }
        }

        // 构建行数据，按列顺序填充
        List<List<Object>> dataList = new ArrayList<>();
        Object rowsObj = task.getRows();
        List<Map<String, Object>> rowsList = rowsObj instanceof List ? (List<Map<String, Object>>) rowsObj : null;
        if (rowsList != null) {
            for (Map<String, Object> row : rowsList) {
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
                .registerWriteHandler(ExcelStyleUtil.CENTER_STYLE)
                .registerWriteHandler(ExcelStyleUtil.createAutoColumnWidthHandler())
                .sheet("数据")
                .doWrite(dataList);
    }

    @Override
    public void exportExcelGroupBy(Long id, Long userId, String groupByField, HttpServletResponse response) throws IOException {
        Task task = getById(id, userId);

        if (groupByField == null || groupByField.trim().isEmpty()) {
            throw new BusinessException(4006, "分组列名不能为空");
        }

        // 校验分组列是否存在
        List<String> headers = new ArrayList<>();
        Object colsObj = task.getColumns();
        if (colsObj instanceof List) {
            for (Object item : (List<?>) colsObj) {
                if (item instanceof ColumnDefine) {
                    headers.add(((ColumnDefine) item).getName());
                } else if (item instanceof Map) {
                    headers.add((String) ((Map<?, ?>) item).get("name"));
                }
            }
        }
        if (!headers.contains(groupByField)) {
            throw new BusinessException(4007, "分组列\"" + groupByField + "\"不存在");
        }

        // 设置响应头
        String fileName = (task.getTitle() != null ? task.getTitle() : "export") + "_分组.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 按分组列的值拆分行数据
        Map<String, List<Map<String, Object>>> groupedRows = new LinkedHashMap<>();
        Object rowsObj = task.getRows();
        List<Map<String, Object>> rowsList = rowsObj instanceof List ? (List<Map<String, Object>>) rowsObj : null;
        if (rowsList != null) {
            for (Map<String, Object> row : rowsList) {
                Object val = row.get(groupByField);
                String key = val != null ? val.toString() : "空";
                groupedRows.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }
        }

        // 表头结构（EasyExcel 格式）
        List<List<String>> headList = headers.stream().map(h -> {
            List<String> head = new ArrayList<>();
            head.add(h);
            return head;
        }).collect(Collectors.toList());

        // 使用 ExcelWriter 写入多个 Sheet
        ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .head(headList)
                .registerWriteHandler(ExcelStyleUtil.CENTER_STYLE)
                .registerWriteHandler(ExcelStyleUtil.createAutoColumnWidthHandler())
                .build();
        int sheetIdx = 0;
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedRows.entrySet()) {
            String sheetName = entry.getKey();
            List<Map<String, Object>> sheetRows = entry.getValue();

            // 构建该 Sheet 的数据
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : sheetRows) {
                List<Object> rowData = new ArrayList<>();
                for (String header : headers) {
                    rowData.add(row.getOrDefault(header, ""));
                }
                dataList.add(rowData);
            }

            WriteSheet writeSheet = EasyExcel.writerSheet(sheetIdx++, sheetName).build();
            writer.write(dataList, writeSheet);
        }
        writer.finish();
    }

    @Override
    public IPage<Task> listPage(int page, int size, String keyword, Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getCreatedAt);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Task::getTitle, keyword.trim());
        }
        if (userId != null) {
            wrapper.eq(Task::getUserId, userId);
        }
        return taskMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public long countAll() {
        return taskMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    public long countToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Task::getCreatedAt, startOfDay, endOfDay);
        return taskMapper.selectCount(wrapper);
    }
}
