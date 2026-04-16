package com.slow.excel_tools_backend.service.impl;

import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.ParseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本解析服务实现类
 */
@Service
public class ParseServiceImpl implements ParseService {

    @Override
    public Task parseText(String text, String delimiter) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(1001, "文本内容不能为空");
        }

        // 将 / 和换行统一作为行分隔符
        String[] segments = text.split("[/\\n\\r]+");

        List<String[]> parsedLines = new ArrayList<>();
        int maxCols = 0;

        for (String segment : segments) {
            segment = segment.trim();
            if (segment.isEmpty()) {
                continue;
            }
            String[] cols = splitLine(segment, delimiter);
            if (cols.length > maxCols) {
                maxCols = cols.length;
            }
            parsedLines.add(cols);
        }

        if (parsedLines.isEmpty()) {
            throw new BusinessException(1002, "未解析到有效数据");
        }

        // 第一行为表头
        String[] header = parsedLines.remove(0);

        // 构建列定义
        List<ColumnDefine> columns = new ArrayList<>();
        for (String h : header) {
            ColumnDefine col = new ColumnDefine();
            col.setName(h.trim());
            col.setType("text");
            columns.add(col);
        }

        // 构建行数据
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String[] cols : parsedLines) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                row.put(columns.get(i).getName(), i < cols.length ? cols[i].trim() : "");
            }
            rows.add(row);
        }

        Task task = new Task();
        task.setColumnsList(columns);
        task.setRowsList(rows);
        return task;
    }

    /**
     * 分隔符处理：优先使用指定分隔符，否则自动识别
     * 支持的列分隔符：+ , ; 空格
     */
    private String[] splitLine(String line, String delimiter) {
        if (delimiter != null && !delimiter.isEmpty() && !delimiter.equals("auto")) {
            if (delimiter.equals("+")) {
                return line.split("\\+");
            } else if (delimiter.equals(" ")) {
                return line.split("\\s+");
            } else {
                return line.split(delimiter);
            }
        }
        // 自动识别列分隔符，优先级：+ , ; 空格
        if (line.contains("+")) {
            return line.split("\\+");
        }
        if (line.contains(",")) {
            return line.split(",");
        }
        if (line.contains(";")) {
            return line.split(";");
        }
        return line.split("\\s+");
    }
}
