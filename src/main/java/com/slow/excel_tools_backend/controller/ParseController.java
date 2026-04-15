package com.slow.excel_tools_backend.controller;

import com.alibaba.excel.EasyExcel;
import com.slow.excel_tools_backend.common.Result;
import com.slow.excel_tools_backend.entity.ColumnDefine;
import com.slow.excel_tools_backend.entity.ExcelParseResult;
import com.slow.excel_tools_backend.entity.Task;
import com.slow.excel_tools_backend.service.ExcelParseService;
import com.slow.excel_tools_backend.service.ParseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据解析控制器
 */
@Api(tags = "数据解析")
@RestController
@RequestMapping("/api/parse")
public class ParseController {

    private final ParseService parseService;
    private final ExcelParseService excelParseService;

    public ParseController(ParseService parseService, ExcelParseService excelParseService) {
        this.parseService = parseService;
        this.excelParseService = excelParseService;
    }

    @ApiOperation("文本智能解析")
    @PostMapping("/text")
    public Result<Map<String, Object>> parseText(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String delimiter = body.get("delimiter");
        Task task = parseService.parseText(text, delimiter);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("columns", task.getColumnsList());
        result.put("data", task.getRowsList());
        return Result.ok(result);
    }

    @ApiOperation("文本解析并直接导出Excel")
    @GetMapping("/text/export")
    public void parseTextAndExport(
            @RequestParam String text,
            @RequestParam(required = false) String delimiter,
            HttpServletResponse response) throws IOException {
        Task task = parseService.parseText(text, delimiter);
        
        exportTaskAsExcel(task, response, "解析数据");
    }

    @ApiOperation("Excel文件导入解析（支持多Sheet）")
    @PostMapping("/excel")
    public Result<ExcelParseResult> parseExcel(MultipartFile file) {
        ExcelParseResult result = excelParseService.parseExcel(file);
        return Result.ok(result);
    }

    @ApiOperation("Excel解析并直接导出Excel")
    @PostMapping("/excel/export")
    public void parseExcelAndExport(MultipartFile file, HttpServletResponse response) throws IOException {
        ExcelParseResult result = excelParseService.parseExcel(file);
        
        if (result.getSheets() != null && !result.getSheets().isEmpty()) {
            exportSheetDataAsExcel(result.getSheets().get(0), response, result.getFileName());
        }
    }

    private void exportTaskAsExcel(Task task, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

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

        List<List<Object>> dataList = new ArrayList<>();
        Object rowsObj = task.getRows();
        if (rowsObj instanceof List) {
            for (Object rowObj : (List<?>) rowsObj) {
                if (rowObj instanceof Map) {
                    Map<String, Object> row = (Map<String, Object>) rowObj;
                    List<Object> rowData = new ArrayList<>();
                    for (String header : headers) {
                        rowData.add(row.getOrDefault(header, ""));
                    }
                    dataList.add(rowData);
                }
            }
        }

        EasyExcel.write(response.getOutputStream())
                .head(headers.stream().map(h -> {
                    List<String> head = new ArrayList<>();
                    head.add(h);
                    return head;
                }).collect(Collectors.toList()))
                .sheet("数据")
                .doWrite(dataList);
    }

    private void exportSheetDataAsExcel(com.slow.excel_tools_backend.entity.SheetData sheetData, 
            HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        List<String> headers = new ArrayList<>();
        if (sheetData.getColumns() != null) {
            for (ColumnDefine col : sheetData.getColumns()) {
                headers.add(col.getName());
            }
        }

        List<List<Object>> dataList = new ArrayList<>();
        if (sheetData.getRows() != null) {
            for (Map<String, Object> row : sheetData.getRows()) {
                List<Object> rowData = new ArrayList<>();
                for (String header : headers) {
                    rowData.add(row.getOrDefault(header, ""));
                }
                dataList.add(rowData);
            }
        }

        EasyExcel.write(response.getOutputStream())
                .head(headers.stream().map(h -> {
                    List<String> head = new ArrayList<>();
                    head.add(h);
                    return head;
                }).collect(Collectors.toList()))
                .sheet(sheetData.getSheetName())
                .doWrite(dataList);
    }
}
