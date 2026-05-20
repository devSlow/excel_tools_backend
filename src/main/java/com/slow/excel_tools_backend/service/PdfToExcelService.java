package com.slow.excel_tools_backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfToExcelService {

    private static final Logger log = LoggerFactory.getLogger(PdfToExcelService.class);

    public byte[] convertToXlsx(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String ext = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf(".") + 1).toLowerCase() : "";

        long start = System.currentTimeMillis();
        log.info("[ToExcel] ====== 开始 {} -> xlsx 转换 ======", ext);
        log.info("[ToExcel] 源文件: {} ({} bytes)", filename, file.getSize());

        switch (ext) {
            case "pdf":
                return convertPdfToXlsx(file, start);
            case "doc":
            case "docx":
                return convertWordToXlsx(file, start);
            default:
                throw new IOException("不支持的输入格式: " + ext + "，仅支持 pdf/doc/docx");
        }
    }

    private byte[] convertPdfToXlsx(MultipartFile file, long start) throws IOException {
        try (PDDocument document = PDDocument.load(file.getBytes())) {
            int totalPages = document.getNumberOfPages();
            log.info("[ToExcel] PDF 页数: {}", totalPages);

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            try (Workbook workbook = new XSSFWorkbook()) {
                for (int page = 1; page <= totalPages; page++) {
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);
                    String pageText = stripper.getText(document);

                    String sheetName = "Page" + page;
                    if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
                    Sheet sheet = workbook.createSheet(sheetName);
                    fillSheet(sheet, pageText, workbook);
                }

                return writeWorkbook(workbook, start, "PDF");
            }
        }
    }

    private byte[] convertWordToXlsx(MultipartFile file, long start) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder fullText = new StringBuilder();
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            log.info("[ToExcel] Word 段落数: {}", paragraphs.size());

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    fullText.append(text).append("\n");
                }
            }

            if (fullText.length() == 0) {
                try (XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    fullText.append(extractor.getText());
                }
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sheet1");
                fillSheet(sheet, fullText.toString(), workbook);
                return writeWorkbook(workbook, start, "Word");
            }
        }
    }

    private void fillSheet(Sheet sheet, String text, Workbook workbook) {
        String[] lines = text.split("\\r?\\n");
        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Row row = sheet.createRow(rowNum++);
            List<String> cells = splitLineToCells(line);
            for (int col = 0; col < cells.size(); col++) {
                Cell cell = row.createCell(col);
                cell.setCellValue(cells.get(col));
                if (rowNum == 1) {
                    cell.setCellStyle(headerStyle);
                }
            }
        }

        for (int col = 0; col < Math.min(20, 20); col++) {
            sheet.autoSizeColumn(col, true);
        }
    }

    private byte[] writeWorkbook(Workbook workbook, long start, String sourceType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        byte[] result = baos.toByteArray();
        long elapsed = System.currentTimeMillis() - start;
        log.info("[ToExcel] {} -> xlsx 成功, {} bytes, 耗时: {}ms", sourceType, result.length, elapsed);
        log.info("[ToExcel] ====== 结束 ======");
        return result;
    }

    private List<String> splitLineToCells(String line) {
        if (line.contains("\t")) {
            List<String> cells = new ArrayList<>();
            for (String part : line.split("\t")) {
                cells.add(part.trim());
            }
            return cells;
        }
        if (line.matches(".*\\s{3,}.*")) {
            List<String> cells = new ArrayList<>();
            for (String part : line.split("\\s{3,}")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    cells.add(trimmed);
                }
            }
            if (!cells.isEmpty()) return cells;
        }
        List<String> single = new ArrayList<>();
        single.add(line.trim());
        return single;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
