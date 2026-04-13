package com.slow.excel_tools_backend.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.slow.excel_tools_backend.common.BusinessException;
import com.slow.excel_tools_backend.config.MinioConfig;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.ExcelParseResult;
import com.slow.excel_tools_backend.entity.SheetData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 读取上传的 Excel 文件，支持多 Sheet 解析，返回每个 Sheet 的 sheetName + columns + rows
 * </p>
 */
@Service
public class ExcelParseService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParseService.class);

    private final MinioService minioService;
    private final MinioConfig minioConfig;

    public ExcelParseService(MinioService minioService, MinioConfig minioConfig) {
        this.minioService = minioService;
        this.minioConfig = minioConfig;
    }

    /**
     * 解析上传的 Excel 文件（多 Sheet）
     * <p>
     * 1. 校验文件格式
     * 2. 上传至 MinIO 备份
     * 3. 遍历所有 Sheet，逐个解析表头和数据
     * 4. 返回 ExcelParseResult（含文件名和各 Sheet 解析结果）
     * </p>
     *
     * @param file 上传的 Excel 文件
     * @return 多 Sheet 解析结果
     */
    public ExcelParseResult parseExcel(MultipartFile file) {
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

        // 获取 Sheet 总数
        int sheetCount;
        try (InputStream is = file.getInputStream()) {
            sheetCount = EasyExcel.read(is).build().excelExecutor().sheetList().size();
        } catch (Exception e) {
            throw new BusinessException(4004, "Excel 文件解析失败");
        }

        // 逐 Sheet 解析
        List<SheetData> sheets = new ArrayList<>();
        for (int i = 0; i < sheetCount; i++) {
            SheetData sheetData = parseSheet(file, i);
            if (sheetData != null) {
                sheets.add(sheetData);
            }
        }

        if (sheets.isEmpty()) {
            throw new BusinessException(4005, "Excel 文件无有效数据");
        }

        ExcelParseResult result = new ExcelParseResult();
        result.setFileName(originalName);
        result.setSheets(sheets);
        return result;
    }

    /**
     * 解析单个 Sheet
     *
     * @param file     Excel 文件
     * @param sheetIdx Sheet 索引（从0开始）
     * @return Sheet 解析结果，无数据时返回 null
     */
    private SheetData parseSheet(MultipartFile file, int sheetIdx) {
        List<Map<Integer, String>> dataList = new ArrayList<>();
        String[] sheetNameHolder = new String[1];

        try (InputStream is = file.getInputStream()) {
            EasyExcel.read(is, new AnalysisEventListener<Map<Integer, String>>() {

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    // 记录表头（第一行）
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    // 首次调用时记录 Sheet 名称
                    if (sheetNameHolder[0] == null) {
                        sheetNameHolder[0] = context.readSheetHolder().getSheetName();
                    }
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Sheet[{}] 解析完成，共 {} 行数据", sheetIdx, dataList.size());
                }
            }).sheet(sheetIdx).doRead();
        } catch (IOException e) {
            log.warn("Sheet[{}] 解析异常", sheetIdx, e);
            return null;
        }

        if (dataList.isEmpty()) {
            return null;
        }

        // 第一行作为表头
        Map<Integer, String> headerRow = dataList.remove(0);
        List<ColumnDefine> columns = buildColumns(headerRow);

        // 构建行数据
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<Integer, String> data : dataList) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int j = 0; j < columns.size(); j++) {
                String val = data.get(j);
                row.put(columns.get(j).getName(), val != null ? val.trim() : "");
            }
            rows.add(row);
        }

        // 空表头且无数据则跳过
        if (columns.isEmpty() && rows.isEmpty()) {
            return null;
        }

        SheetData sheetData = new SheetData();
        sheetData.setSheetName(sheetNameHolder[0] != null ? sheetNameHolder[0] : "Sheet" + (sheetIdx + 1));
        sheetData.setColumns(columns);
        sheetData.setRows(rows);
        return sheetData;
    }

    /**
     * 根据表头行构建列定义列表
     */
    private List<ColumnDefine> buildColumns(Map<Integer, String> headerRow) {
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
        columns.removeIf(c -> c == null);
        return columns;
    }
}
