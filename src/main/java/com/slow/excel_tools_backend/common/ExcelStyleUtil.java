package com.slow.excel_tools_backend.common;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class ExcelStyleUtil {

    public static final HorizontalCellStyleStrategy CENTER_STYLE;

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

    public static WorkbookWriteHandler createAutoColumnWidthHandler() {
        return new AutoColumnWidthHandler();
    }

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
            if (lastRowNum < firstRowNum) return;

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
                int width = (maxLen + 4) * 256;
                if (width > 18000) width = 18000;
                if (width < 2000) width = 2000;
                sheet.setColumnWidth(c, width);
            }
        }
    }
}