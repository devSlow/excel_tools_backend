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

        // 按行拆分
        String[] lines = text.split("\n");

        // 查找第一个包含 / 的行，按 / 分割
        int splitIndex = -1;
        String[] headerLine = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && line.contains("/")) {
                splitIndex = i;
                int firstSlashIndex = line.indexOf("/");
                String headerPart = line.substring(0, firstSlashIndex).trim();
                headerLine = splitLine(headerPart, delimiter);
                headerLine = fixSingleCharName(headerLine);
                break;
            }
        }

        int hasSlash = splitIndex >= 0 ? 1 : 0;
        List<String[]> parsedLines = new ArrayList<>();
        int maxCols = 0;

        if (splitIndex >= 0) {
            // 有 / ，按第一个 / 分割：前面的为表头，后面的为数据
            maxCols = headerLine.length;

            // 从包含 / 的行提取 / 后面的数据部分
            String lineWithSlash = lines[splitIndex].trim();
            int firstSlashIndex = lineWithSlash.indexOf("/");
            String dataPart = lineWithSlash.substring(firstSlashIndex + 1).trim();
            if (!dataPart.isEmpty()) {
                String[] dataCols = splitLine(dataPart, delimiter);
                dataCols = fixSingleCharName(dataCols);
                if (dataCols.length > maxCols) {
                    maxCols = dataCols.length;
                }
                parsedLines.add(dataCols);
            }

            // 处理剩余的行
            for (int i = splitIndex + 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] cols = splitLine(line, delimiter);
                cols = fixSingleCharName(cols);
                if (cols.length > maxCols) {
                    maxCols = cols.length;
                }
                parsedLines.add(cols);
            }
        } else {
            // 没有 / ，原逻辑：第一行为表头，后面的为数据
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] cols = splitLine(line, delimiter);
                cols = fixSingleCharName(cols);
                if (cols.length > maxCols) {
                    maxCols = cols.length;
                }
                parsedLines.add(cols);
            }
        }

        if (parsedLines.isEmpty()) {
            throw new BusinessException(1002, "未解析到有效数据");
        }

        // 构建列定义
        List<ColumnDefine> columns = new ArrayList<>();
        if (maxCols == 1) {
            ColumnDefine col = new ColumnDefine();
            col.setName("姓名");
            col.setType("text");
            columns.add(col);
        } else {
            if (hasSlash == 1 && headerLine != null) {
                for (String h : headerLine) {
                    ColumnDefine col = new ColumnDefine();
                    col.setName(h.trim());
                    col.setType("text");
                    columns.add(col);
                }
            } else {
                String[] header = parsedLines.get(0);
                for (String h : header) {
                    ColumnDefine col = new ColumnDefine();
                    col.setName(h.trim());
                    col.setType("text");
                    columns.add(col);
                }
                parsedLines.remove(0);
            }
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
     * 修复单字符名字被错误拆分的问题
     * 如果第一列是单字符（可能是名字的一部分），则合并后续列
     */
    private String[] fixSingleCharName(String[] cols) {
        if (cols.length < 2) {
            return cols;
        }
        
        String first = cols[0].trim();
        // 检查第一列是否是单个中文字符
        if (first.length() == 1 && isChineseChar(first.charAt(0))) {
            // 尝试与后续列合并形成完整名字（名字通常是2-3个字符）
            // 合并最多到第三列（假设最多3个字的名字）
            StringBuilder merged = new StringBuilder(first);
            int mergeCount = 1;
            for (int i = 1; i < Math.min(cols.length, 3) && mergeCount < 3; i++) {
                String next = cols[i].trim();
                // 如果后续列仍然很短（可能是名字的一部分），继续合并
                if (next.length() <= 2 && isLikelyNamePart(next)) {
                    merged.append(next);
                    mergeCount++;
                } else {
                    break;
                }
            }
            
            if (mergeCount > 1) {
                String[] fixed = new String[cols.length - mergeCount + 1];
                fixed[0] = merged.toString();
                System.arraycopy(cols, mergeCount, fixed, 1, cols.length - mergeCount);
                return fixed;
            }
        }
        
        return cols;
    }
    
    /**
     * 判断字符是否为中文
     */
    private boolean isChineseChar(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
    
    /**
     * 判断字符串是否可能是名字的一部分
     */
    private boolean isLikelyNamePart(String s) {
        if (s.isEmpty()) return false;
        // 检查是否全是中文或者很短（1-2个字符）
        if (s.length() <= 2) {
            for (char c : s.toCharArray()) {
                if (isChineseChar(c) || Character.isLetterOrDigit(c)) {
                    continue;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 分隔符处理：优先使用指定分隔符，否则自动识别
     */
    private String[] splitLine(String line, String delimiter) {
        if (delimiter != null && !delimiter.isEmpty() && !delimiter.equals("auto")) {
            if (delimiter.equals("\\t")) {
                return line.split("\t");
            } else if (delimiter.equals(" ")) {
                return line.split("\\s+");
            } else {
                return line.split(delimiter);
            }
        }
        // 自动识别分隔符
        if (line.contains("\t")) {
            return line.split("\t");
        }
        if (line.contains("+")) {
            return line.split("\\+");
        }
        if (line.contains(",")) {
            return line.split(",");
        }
        if (line.contains(";")) {
            return line.split(";");
        }
        // 全角空格作为分隔符
        if (line.contains("\u3000")) {
            return line.split("\u3000+");
        }
        // 多个连续空格作为分隔符
        if (line.contains("  ")) {
            return line.split("\\s{2,}");
        }
        // 单个空格分隔
        return line.split(" ");
    }
}
