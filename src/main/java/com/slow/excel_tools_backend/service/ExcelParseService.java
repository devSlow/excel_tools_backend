package com.slow.excel_tools_backend.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.config.MinioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Excel 文件解析服务
 * <p>
 * 读取上传的 Excel 文件，解析为统一的 columns + rows 结构
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelParseService {

    private final MinioService minioService;
    private final MinioConfig minioConfig;

    /**
     * 解析上传的 Excel 文件
     * <p>
     * 1. 校验文件格式
     * 2. 上传至 MinIO 备份
     * 3. 使用 EasyExcel 读取表头和数据
     * 4. 转换为 columns + rows 结构返回
     * </p>
     *
     * @param file 上传的 Excel 文件
     * @return 解析后的任务结构
     */
    public Task parseExcel(MultipartFile file) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4001, "上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls"))) {
            throw new BusinessException(4002, "仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }

        // 上传至 MinIO 备份
        String objectName = "excel/" + UUID.randomUUID() + "/" + originalName;
        try {
            minioService.upload(minioConfig.getBucket(), objectName,
                    file.getInputStream(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new BusinessException(4003, "文件读取失败");
        }

        // 使用 EasyExcel 解析
        List<Map<Integer, String>> dataList = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            EasyExcel.read(is, new AnalysisEventListener<Map<Integer, String>>() {

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    // 表头行也会触发 invoke，第一行即为表头
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 解析完成，共 {} 行数据", dataList.size());
                }
            }).sheet().doRead();
        } catch (IOException e) {
            throw new BusinessException(4004, "Excel 文件解析失败");
        }

        if (dataList.isEmpty()) {
            throw new BusinessException(4005, "Excel 文件无有效数据");
        }

        // 第一行作为表头，构建列定义
        Map<Integer, String> headerRow = dataList.remove(0);
        List<ColumnDefine> columns = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : headerRow.entrySet()) {
            ColumnDefine col = new ColumnDefine();
            col.setName(entry.getValue() != null ? entry.getValue().trim() : "列" + (entry.getKey() + 1));
            col.setType("text");
            // 保证列顺序
            while (columns.size() <= entry.getKey()) {
                columns.add(null);
            }
            columns.set(entry.getKey(), col);
        }
        // 移除空位
        columns.removeIf(c -> c == null);

        // 构建行数据
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<Integer, String> data : dataList) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                String val = data.get(i);
                row.put(columns.get(i).getName(), val != null ? val.trim() : "");
            }
            rows.add(row);
        }

        Task task = new Task();
        task.setTitle(originalName.substring(0, originalName.lastIndexOf(".")));
        task.setColumns(columns);
        task.setRows(rows);
        return task;
    }
}
