package com.slow.excel_tools_backend.service;

import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParseService {

    public Task parseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }

        String[] lines = text.split("\n");
        List<String[]> parsedLines = new ArrayList<>();
        int maxCols = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] cols = splitLine(line);
            if (cols.length > maxCols) {
                maxCols = cols.length;
            }
            parsedLines.add(cols);
        }

        if (parsedLines.isEmpty()) {
            throw new IllegalArgumentException("未解析到有效数据");
        }

        // 如果只有一列，说明每行只有一个值（如名单），列名默认"姓名"
        List<ColumnDefine> columns = new ArrayList<>();
        if (maxCols == 1) {
            columns.add(new ColumnDefine() {{
                setName("姓名");
                setType("text");
            }});
        } else {
            // 第一行作为表头
            String[] headerLine = parsedLines.get(0);
            for (String h : headerLine) {
                ColumnDefine col = new ColumnDefine();
                col.setName(h.trim());
                col.setType("text");
                columns.add(col);
            }
            parsedLines.remove(0);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (String[] cols : parsedLines) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                row.put(columns.get(i).getName(), i < cols.length ? cols[i].trim() : "");
            }
            rows.add(row);
        }

        Task task = new Task();
        task.setColumns(columns);
        task.setRows(rows);
        return task;
    }

    private String[] splitLine(String line) {
        // 优先按 Tab 分割，其次多空格，其次逗号
        if (line.contains("\t")) {
            return line.split("\t");
        }
        if (line.contains(",")) {
            return line.split(",");
        }
        // 多个连续空格作为分隔符，但保留单个空格的值（如人名中间可能没空格）
        // 策略：2个及以上连续空格分列
        if (line.contains("  ")) {
            return line.split("\\s{2,}");
        }
        // 单空格分割
        return line.split("\\s+");
    }
}
