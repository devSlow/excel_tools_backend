package com.slow.excel_tools_backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slow.excel_tools_backend.common.BusinessException;
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

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * 数据任务服务实现类
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final HorizontalCellStyleStrategy CENTER_STYLE;

    static {
        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        WriteFont headFont = new WriteFont();
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);

        CENTER_STYLE = new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 自适应列宽处理器：遍历每列取最大字符宽度，加上余量后设置列宽
     */
    private static class AutoColumnWidthHandler implements WorkbookWriteHandler {

        @Override
        public void afterWorkbookDispose(WriteWorkbookHolder writeWorkbookHolder) {
            org.apache.poi.ss.usermodel.Workbook workbook = writeWorkbookHolder.getWorkbook();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                autoSizeSheet(sheet);
            }
        }

        private void autoSizeSheet(Sheet sheet) {
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < firstRowNum) {
                return;
            }

            int maxCol = 0;
            for (int r = firstRowNum; r <= lastRowNum; r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                if (row != null && row.getLastCellNum() > maxCol) {
                    maxCol = row.getLastCellNum();
                }
            }

            for (int c = 0; c < maxCol; c++) {
                int maxLen = 0;
                for (int r = firstRowNum; r <= lastRowNum; r++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                    if (row == null) continue;
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                    if (cell == null) continue;
                    String val = cell.getStringCellValue();
                    if (val == null) continue;
                    int len = 0;
                    for (char ch : val.toCharArray()) {
                        len += (ch > 127) ? 2 : 1;
                    }
                    if (len > maxLen) maxLen = len;
                }
                // 加4个字符余量，避免太挤
                int width = (maxLen + 4) * 256;
                if (width > 18000) width = 18000;
                if (width < 2000) width = 2000;
                sheet.setColumnWidth(c, width);
            }
        }
    }

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
                .registerWriteHandler(CENTER_STYLE)
                .registerWriteHandler(new AutoColumnWidthHandler())
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
        if (task.getColumns() != null) {
            for (ColumnDefine col : task.getColumns()) {
                headers.add(col.getName());
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
        if (task.getRows() != null) {
            for (Map<String, Object> row : task.getRows()) {
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
                .registerWriteHandler(CENTER_STYLE)
                .registerWriteHandler(new AutoColumnWidthHandler())
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
}
